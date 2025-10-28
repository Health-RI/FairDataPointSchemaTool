package nl.healthri.fdp.uploadschema.integration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FdpService {
    private final IFdpClient fdpClient;

    @Autowired
    public FdpService(IFdpClient fdpClient) {
        this.fdpClient = fdpClient;
    }
}

