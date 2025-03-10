package transferdata;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

class Version {

    private static final Pattern pattern = Pattern.compile("(\\d+)\\.(\\d+)\\.(\\d+)");
    int major;
    int minor;
    int patch;

    public Version() {
        new Version(1, 0, 0);
    }

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

    private Version(int a, int b, int c) {
        major = a;
        minor = b;
        patch = c;
    }

    public String toString() {
        return major + "." + minor + "." + patch;
    }

    Version next() {
        return new Version(major, minor, patch + 1);
    }
}
