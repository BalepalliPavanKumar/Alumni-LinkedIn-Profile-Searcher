package com.alumni.linkedinsearcher.phantombuster;

import com.alumni.linkedinsearcher.dto.AlumniSearchRequest;
import com.alumni.linkedinsearcher.phantombuster.dto.PhantomBusterProfileDto;

import java.util.List;

/**
 * Adapter over the PhantomBuster REST API. Isolated behind an interface so
 * the rest of the application (and its tests) never depend on the concrete
 * HTTP/polling mechanics of the third-party service.
 */
public interface PhantomBusterClient {

    List<PhantomBusterProfileDto> searchLinkedInProfiles(AlumniSearchRequest request);
}
