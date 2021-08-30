package com.github.rzo1;

public enum MailType {
        MAIL_HTML ("text/html"),
        MAIL_PLAIN ("text/plain");
        private String mimeType;

        MailType(String mimeType) {
            this.mimeType = mimeType;
        }

        public String getMimeType() {
            return mimeType;
        }

        public static MailType fromMimeType(String mimeType) {
            if (mimeType != null) {
                for (MailType t : MailType.values()) {
                    if (mimeType.equalsIgnoreCase(t.mimeType)) {
                        return t;
                    }
                }
            }
            return null;
        }
    }
