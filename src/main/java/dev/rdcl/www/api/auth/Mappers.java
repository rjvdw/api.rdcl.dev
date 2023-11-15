package dev.rdcl.www.api.auth;

import com.yubico.webauthn.data.ByteArray;
import com.yubico.webauthn.data.UserIdentity;
import dev.rdcl.www.api.auth.entities.Identity;

import java.nio.ByteBuffer;
import java.util.UUID;

public class Mappers {

    public static UserIdentity identityToUserIdentity(Identity identity) {
        return UserIdentity.builder()
                .name(identity.getEmail())
                .displayName(identity.getName())
                .id(uuidToByteArray(identity.getId()))
                .build();
    }

    public static UUID byteArrayToUuid(ByteArray ba) {
        byte[] bytes = ba.getBytes();
        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
        long high = byteBuffer.getLong();
        long low = byteBuffer.getLong();

        return new UUID(high, low);
    }

    public static ByteArray uuidToByteArray(UUID uuid) {
        ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
        bb.putLong(uuid.getMostSignificantBits());
        bb.putLong(uuid.getLeastSignificantBits());

        return new ByteArray(bb.array());
    }
}
