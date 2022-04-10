package security.ttaallkk.domain.notification;


public enum NotificationType {
    COMMENT("comment"),
    CHILDRENCOMMENT("childrencomment");

    private final String typeName;
    
    NotificationType(String typeName) {
        this.typeName = typeName;
    }

    public String getTypeName() {
        return typeName;
    }
}
