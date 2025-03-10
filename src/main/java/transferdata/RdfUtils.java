package schematool;

import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.http.HTTPRepository;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.sail.nativerdf.NativeStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicInteger;

public class RdfUtils {
    private static final Logger logger = LoggerFactory.getLogger(RdfUtils.class);

    private RdfUtils() {
        //prevents instantiation
    }

    //default access, so unittest can access.
    static ConnectionProperties parseUrl(URL url) {
        String path = url.getFile();
        String repo = path.substring(path.lastIndexOf('/') + 1);
        ;
        if (url.getUserInfo() != null) {
            var parts = url.getUserInfo().split(":");
            if (parts.length == 2) {
                return new ConnectionProperties(urlString(url), parts[0], parts[1], repo);
            }
            logger.warn("Invalid username / password in url");
        }
        return new ConnectionProperties(urlString(url), null, null, repo);
    }

    private static String urlString(URL url) {
        StringBuilder sb = new StringBuilder(url.getProtocol());
        sb.append("://");
        sb.append(url.getHost());
        if (url.getPort() != -1) {
            sb.append(":");
            sb.append(url.getPort());
        }
        sb.append(url.getPath());
        return sb.toString();
    }


    public static RepositoryConnection tripleStore(Path dataDir) {
        var repo = new SailRepository(new NativeStore(dataDir.toFile()));
        repo.init();
        return repo.getConnection();
    }

    public static RepositoryConnection tripleStore(URL url) {
        var p = parseUrl(url);
        var repo = new HTTPRepository(p.url());
        // Set the username and password for basic authentication
        if (p.username() != null && p.password() != null) {
            repo.setUsernameAndPassword(p.username(), p.password());
        }
        repo.init();
        return repo.getConnection();
    }

    public static void clearRepository(RepositoryConnection repoConnection) {
        logger.info("Clear repository it currently has {} triples", repoConnection.size());
        repoConnection.begin();
        repoConnection.clear();
        repoConnection.commit();
    }

    public static void printStats(RepositoryConnection repoConnection) {
        try (var ctxs = repoConnection.getContextIDs()) {
            for (var r : ctxs) {
                repoConnection.getStatements(null, null, null, true, r)
                        .stream().forEach(System.out::println);
            }
            repoConnection.getStatements(null, null, null, true, (Resource) null)
                    .stream().forEach(System.out::println);
        }
    }

    public static void copy(RepositoryConnection sourceConnection, RepositoryConnection targetConnection) {
        final AtomicInteger i = new AtomicInteger(0);
        targetConnection.begin();
        try (var ctxs = sourceConnection.getContextIDs()) {
            for (var r : ctxs) {
                sourceConnection.getStatements(null, null, null, true, r)
                        .stream().forEach(st -> {
                            targetConnection.add(st);
                            i.incrementAndGet();
                        });
            }
            sourceConnection.getStatements(null, null, null, true, (Resource) null)
                    .stream().forEach(st -> {
                        targetConnection.add(st);
                        i.incrementAndGet();
                    });
        }
        targetConnection.commit();

        System.out.println("added: " + i.get() + " triples");
        logger.info("target now has : {} triples", targetConnection.size());
        System.out.printf("target now has : %s triples%n", targetConnection.size());
    }

    record ConnectionProperties(String url, String username, String password, String repo) {
    }
}
