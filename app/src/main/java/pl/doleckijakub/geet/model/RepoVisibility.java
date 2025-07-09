package pl.doleckijakub.geet.model;

public enum RepoVisibility {
    PUBLIC,
    PRIVATE,
    UNLISTED;

    public static RepoVisibility fromString(String sVisibility) {
        for (RepoVisibility visibility : values())
            if (sVisibility.toUpperCase().equals(visibility.toString()))
                return visibility;

        return null;
    }
}
