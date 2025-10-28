package nl.healthri.fdp.uploadschema.integration;

@Service
public class FdpService {
    private final IFdpClient fdpClient;

    @Autowired
    public FdpService(IFdpClient fdpClient) {
        this.fdpClient = fdpClient;
    }
}

