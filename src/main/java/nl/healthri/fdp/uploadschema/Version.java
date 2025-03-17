package nl.healthri.fdp.uploadschema;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Version {

    private static final Pattern pattern = Pattern.compile("(\\d+)\\.(\\d+)\\.(\\d+)");
    int major;
    int minor;
    int patch;

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

    public Version next(int requestedMajorVersion) {
        if (requestedMajorVersion > major) {
            return new Version(requestedMajorVersion, 0, 0);
        }
        return new Version(major, minor, patch + 1);
    }

    public String major() {
        return Integer.toString(major);
    }

    public String minor() {
        return Integer.toString(minor);
    }

    public String patch() {
        return Integer.toString(patch);
    }
}
