/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package cloud.cleo.connectgpt;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.CloudFormationCustomResourceEvent;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import software.amazon.awssdk.auth.credentials.EnvironmentVariableCredentialsProvider;
import software.amazon.awssdk.core.SdkSystemSetting;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.polly.PollyClient;
import software.amazon.awssdk.services.polly.model.Engine;
import software.amazon.awssdk.services.polly.model.OutputFormat;
import software.amazon.awssdk.services.polly.model.SynthesizeSpeechRequest;
import software.amazon.awssdk.services.polly.model.TextType;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.lambda.powertools.cloudformation.AbstractCustomResourceHandler;
import software.amazon.lambda.powertools.cloudformation.Response;

/**
 *
 * @author sjensen
 */
public class PollyPromptGenerator extends AbstractCustomResourceHandler {

    // Initialize the Log4j logger.
    Logger log = LogManager.getLogger(PollyPromptGenerator.class);

    private final static PollyClient polly = PollyClient.builder()
            .region(Region.of(System.getenv(SdkSystemSetting.AWS_REGION.environmentVariable())))
            .credentialsProvider(EnvironmentVariableCredentialsProvider.create())
            .httpClient(UrlConnectionHttpClient.builder().build())
            .build();

    private final static S3Client s3 = S3Client.builder()
            .region(Region.of(System.getenv(SdkSystemSetting.AWS_REGION.environmentVariable())))
            .credentialsProvider(EnvironmentVariableCredentialsProvider.create())
            .httpClient(UrlConnectionHttpClient.builder().build())
            .build();

    private final static String BUCKET_NAME = System.getenv("PROMPT_BUCKET");

    @Override
    protected Response create(CloudFormationCustomResourceEvent cfcre, Context cntxt) {
        log.debug("Received CREATE Event from Cloudformation");
        log.debug(cfcre);

        try {
            final var name = cfcre.getResourceProperties().get("PromptName").toString();
            final var text = cfcre.getResourceProperties().get("PromptText").toString();
            final var voice_id = cfcre.getResourceProperties().get("VoiceId").toString();

            final var ssr = SynthesizeSpeechRequest.builder()
                    .engine(Engine.NEURAL)
                    .voiceId(voice_id)
                    .sampleRate("8000")
                    .outputFormat(OutputFormat.PCM)
                    .textType(TextType.TEXT)
                    .text(text).build();

            final var por = PutObjectRequest.builder()
                    .bucket(BUCKET_NAME)
                    //.contentType("audio/wav")
                    .key(name)
                    .build();

            // get the task root which is where all the resources will be (sox binary)
            final var task_root = System.getenv("LAMBDA_TASK_ROOT");

                        
            // Sox binary is in the resource folder
            final var soxBinary = Path.of(task_root, "sox");
            log.debug("LD_LIBRARY_PATH=" + System.getenv("LD_LIBRARY_PATH"));
            

            // Name of temp for input to sox
            final var pollyFile = Path.of("/tmp", "polly_audio.pcm");
            
            // Name of temp for outout of sox
            final var wavFile = Path.of("/tmp", name );
            
            // Take the Polly output and write to temp file
            Files.copy(polly.synthesizeSpeech(ssr), pollyFile, StandardCopyOption.REPLACE_EXISTING);

            // Call sox to convert PCM file to WAV Ulaw which is required for connect prompts
            // https://docs.aws.amazon.com/connect/latest/adminguide/setup-prompts-s3.html
            final var command = String.format("%s -t raw -r 8000 -e signed -b 16 -c 1 %s -r 8000 -c 1 -e u-law %s", soxBinary, pollyFile, wavFile);
            log.debug("Executing: " + command);
            final var process = Runtime.getRuntime().exec(command);
            

            final var inStream = new StreamGobbler(process.getInputStream(), log::debug);
            Executors.newSingleThreadExecutor().submit(inStream);
            
            final var errorStream = new StreamGobbler(process.getErrorStream(), log::debug);
            Executors.newSingleThreadExecutor().submit(errorStream);
            
            log.debug(" Process exited with " + process.waitFor());
            
            
            // Push the final wav file into the prompt bucket
            s3.putObject(por, RequestBody.fromFile(wavFile));

        } catch (Exception e) {
            log.error("Could Not create the prompt", e);
        }
        return Response.builder()
                .value(cfcre.getResourceProperties())
                .build();
    }

    private static class StreamGobbler implements Runnable {

        private final InputStream inputStream;
        private final Consumer<String> consumer;

        public StreamGobbler(InputStream inputStream, Consumer<String> consumer) {
            this.inputStream = inputStream;
            this.consumer = consumer;
        }

        @Override
        public void run() {
            new BufferedReader(new InputStreamReader(inputStream)).lines()
                    .forEach(consumer);
        }
    }

    

    /**
     * We don't do anything on stack updates, just return null
     *
     * @param cfcre
     * @param cntxt
     * @return
     */
    @Override
    protected Response update(CloudFormationCustomResourceEvent cfcre, Context cntxt) {
        log.debug("Received UPDATE Event from Cloudformation", cfcre);
        return null;
    }

    /**
     * Delete the prompts we created so the bucket can be deleted
     *
     * @param cfcre
     * @param cntxt
     * @return
     */
    @Override
    protected Response delete(CloudFormationCustomResourceEvent cfcre, Context cntxt) {
        try {
            final var name = cfcre.getResourceProperties().get("PromptName").toString();
            log.debug("Deleting Promp " + name);
            final var dor = DeleteObjectRequest.builder()
                    .bucket(BUCKET_NAME)
                    .key(name)
                    .build();

            s3.deleteObject(dor);

        } catch (Exception e) {
            log.error("Could Not delete the prompt", e);
        }
        return Response.builder()
                .value(cfcre.getResourceProperties())
                .build();
    }

}
