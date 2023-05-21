/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.github.softwarebymark.lex.domain;

import com.github.softwarebymark.lex.domain.action.DialogAction;
import java.util.List;

/**
 *
 * @author sjensen
 */
public class SessionStateResponse extends SessionState {

    private DialogAction dialogAction;
    private Intent intent;

    public SessionStateResponse(DialogAction dialogAction, Intent intent) {
        this.dialogAction = dialogAction;
        this.intent = intent;
    }

    public DialogAction getDialogAction() {
        return dialogAction;
    }


    public Intent getIntent() {
        return intent;
    }
}
