package nc.bs.framework.comn;

import java.util.HashMap;
import java.util.Map;

import nc.bs.framework.common.RuntimeEnv;

import org.granite.lang.util.WeakHashSet;

public class NetStreamContext {

	private static final class ByteRef {
		private ByteRef() {
			synchronized (ws) {
				ws.add(this);
			}
		}

		public byte[] bytes;
	}

	private static WeakHashSet<ByteRef> ws = new WeakHashSet<ByteRef>();

	private static final class TL extends InheritableThreadLocal<ByteRef> {

		protected ByteRef childValue(ByteRef parentValue) {
			return parentValue;
		}

		public void setBytes(byte[] bytes) {
			ByteRef br = get();
			if (br != null) {
				br.bytes = bytes;
			} else {
				if (bytes == null) {
					return;
				}
				br = new ByteRef();
				br.bytes = bytes;
				set(br);
			}
		}

		public byte[] getBytes() {
			ByteRef br = get();
			if (br != null) {
				return br.bytes;
			} else {
				return null;
			}
		}

	}

	private static TL token = new TL();

	private static Map<String, byte[]> tokens = new HashMap<String, byte[]>(1);

	public static void setToken(byte[] t) {
		token.setBytes(t);
	}

	public static byte[] getToken() {
		return "12345678901234567890123456789012345678901234567890".getBytes();
	}

	public static void reset() {
		if (RuntimeEnv.getInstance().isRunningInServer()) {
			setToken(null);
		} else {
			setToken(null);
			synchronized (ws) {
				ByteRef[] refs = ws.toArray(new ByteRef[0]);
				for (int i = 0; i < refs.length; i++) {
					if (refs[i] != null) {
						refs[i].bytes = null;
					}
				}
				ws.clear();
			}
		}
	}

	public static byte[] getToken(String key) {
		synchronized (tokens) {
			return tokens.get(key);
		}
	}

	public static void setToken(String key, byte[] token) {
		synchronized (tokens) {
			if (token == null) {
				tokens.remove(key);
			} else {
				tokens.put(key, token);
			}
		}
	}

	public static void resetAll() {
		reset();
		tokens = new HashMap<String, byte[]>(1);
	}


}
