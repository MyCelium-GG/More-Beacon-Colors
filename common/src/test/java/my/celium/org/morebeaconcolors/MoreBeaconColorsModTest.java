package my.celium.org.morebeaconcolors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import my.celium.org.Mycel;
import my.celium.org.internal.PlatformHolder;
import my.celium.org.morebeaconcolors.client.BeamButton;

/** The mod boots on Mycel with a small, well-defaulted config and both packet definitions. */
class MoreBeaconColorsModTest {
    @BeforeAll
    static void initOnce(@TempDir Path tempDir) {
        PlatformHolder.setForTesting(new TestPlatform(tempDir));
        MoreBeaconColorsMod.init();
    }

    @Test
    void registersWithMycel() {
        assertTrue(Mycel.getRegisteredMods().contains(MoreBeaconColorsMod.META));
        assertEquals("morebeaconcolors", MoreBeaconColorsMod.META.id());
    }

    @Test
    void configDefaults() {
        assertTrue(MoreBeaconColorsMod.enabled.get());
        assertTrue(MoreBeaconColorsMod.showButton.get());
        assertTrue(MoreBeaconColorsMod.allowAnimated.get());
        assertFalse(MoreBeaconColorsMod.debug.get());
    }

    @Test
    void packetsAreNamespaced() {
        assertNotNull(MoreBeaconColorsMod.setBeam);
        assertNotNull(MoreBeaconColorsMod.beams);
        assertEquals("morebeaconcolors", MoreBeaconColorsMod.setBeam.id().getNamespace());
        assertEquals("morebeaconcolors", MoreBeaconColorsMod.beams.id().getNamespace());
    }

    @Test
    void hooksTolerateNulls() {
        assertNull(BeamButton.create(null));
    }
}
