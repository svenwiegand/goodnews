package com.gettingmobile.goodnews.download;

import com.gettingmobile.ApplicationException;
import com.gettingmobile.goodnews.R;

public class DownloadException extends ApplicationException {
    public enum ErrorCode {
        STORAGE_NOT_AVAILABLE,
        UNEXPECTED_RESOURCE_TYPE,
        GENERIC
    }

    public DownloadException(ErrorCode errorCode) {
        super(errorCode);
    }

    public DownloadException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }

    protected DownloadException(Enum<?> errorCode, String detailMessage) {
        super(errorCode, detailMessage);
    }

    protected DownloadException(Enum<?> errorCode, String detailMessage, Throwable cause) {
        super(errorCode, detailMessage, cause);
    }

    public int getErrorStringId() {
        switch (ErrorCode.values()[errorCode.ordinal()]) {
            case STORAGE_NOT_AVAILABLE:
                return R.string.download_error_storage_not_available;
            default:
                return R.string.download_error_generic;
        }
    }
}
