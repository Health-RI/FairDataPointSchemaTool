package nl.healthri.fdp.uploadschema.config;

public class SettingsTest {

    public void DuplicateRdfTypeInFdpSettings_WhenMerging_ReturnsDuplicateKeyException(){}

    public void LocalSourceFoundInFdp_WhenMerging_ReturnsSettingsWithFdpSource(){}

    public void LocalSourceMissingInFdpSettings_WhenMerging_ReturnsSettingsWithLocalSource(){}

    public void FileNotFound_WhenGettingSettings_ThrowsFileNotFoundException(){}

    public void MalformedJsonFile_WhenGettingSettings_ThrowsIOException(){}

    public void ValidJsonFile_WhenGettingSettings_ReturnsSettings(){

    }
}
