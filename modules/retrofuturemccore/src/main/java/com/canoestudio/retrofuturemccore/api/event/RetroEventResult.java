package com.canoestudio.retrofuturemccore.api.event;

import net.minecraft.util.EnumActionResult;

public enum RetroEventResult {
    PASS(EnumActionResult.PASS, false),
    SUCCESS(EnumActionResult.SUCCESS, true),
    FAIL(EnumActionResult.FAIL, true);

    private final EnumActionResult actionResult;
    private final boolean handled;

    RetroEventResult(EnumActionResult actionResult, boolean handled) {
        this.actionResult = actionResult;
        this.handled = handled;
    }

    public EnumActionResult getActionResult() {
        return this.actionResult;
    }

    public boolean isHandled() {
        return this.handled;
    }

    public static RetroEventResult fromActionResult(EnumActionResult result) {
        if (result == EnumActionResult.SUCCESS) {
            return SUCCESS;
        }
        if (result == EnumActionResult.FAIL) {
            return FAIL;
        }
        return PASS;
    }
}
