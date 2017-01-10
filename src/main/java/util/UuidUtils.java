package util;

import java.nio.ByteBuffer;
import java.util.UUID;

public abstract class UuidUtils {

	public static String base58Uuid() {
		UUID uuid = UUID.randomUUID();
		String temp = base58Uuid(uuid);
		while (temp.matches("[0-9].*")) {
			uuid = UUID.randomUUID();
			temp = base58Uuid(uuid);
		}
		return base58Uuid(uuid);
	}

	public static void main(String[] args) {
		for (int i = 0; i < 100; i++) {
			System.out.println(base58Uuid());
		}
	}

	private static String base58Uuid(UUID uuid) {
		ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
		bb.putLong(uuid.getMostSignificantBits());
		bb.putLong(uuid.getLeastSignificantBits());
		return Base58.encode(bb.array());
	}

}
