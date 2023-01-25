package org.processmining.stochasticlabelleddatapetrinet.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.processmining.stochasticlabelleddatapetrinet.preprocess.OneHotEncoding;

public class OneHotEncodingSerializerImpl implements OneHotEncodingSerializer {

	public void serialize(OneHotEncoding net, OutputStream os) throws IOException {
		net.serialize(os);
	}

	public OneHotEncoding deserialize(InputStream is) throws IOException, ClassNotFoundException {
		OneHotEncoding oneHotEncoding = new OneHotEncoding();
		oneHotEncoding.deserialize(is);
		return oneHotEncoding;
	}

}
