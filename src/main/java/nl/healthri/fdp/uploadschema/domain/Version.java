package nl.healthri.fdp.uploadschema.domain;

import java.util.Comparator;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Version implements Comparable<Version> {

    private static final Pattern pattern = Pattern.compile("(\\d+)\\.(\\d+)\\.(\\d+)");
    final private int major;
    final private int minor;
    final private int patch;

    public Version(String version) {
        Matcher matcher = pattern.matcher(version);
        if (matcher.matches()) {
            major = Integer.parseInt(matcher.group(1));
            minor = Integer.parseInt(matcher.group(2));
            patch = Integer.parseInt(matcher.group(3));
        } else {
            throw new IllegalArgumentException("Version: " + version + " is not valid format");
        }
    }

    public Version(int major, int minor, int patch) {
        this.major = major;
        this.minor = minor;
        this.patch = patch;
    }

    public String toString() {
        return major + "." + minor + "." + patch;
    }

    public Version next(Version requestedVersion) {
        if (requestedVersion.compareTo(this) > 0) {
            return requestedVersion;
        }
        return new Version(major, minor, patch + 1);
    }

    public int major() {
        return major;
    }

    public int minor() {
        return minor;
    }

    public int patch() {
        return patch;
    }

    @Override
    public int compareTo(Version other) {
        return Comparator.comparingInt(Version::major)
                .thenComparingInt(Version::minor)
                .thenComparingInt(Version::patch)
                .compare(this, other);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Version version)) return false;
        return major == version.major && minor == version.minor && patch == version.patch;
    }

    @Override
    public int hashCode() {
        return Objects.hash(major, minor, patch);
    }
}
