package org.processmining.stochasticlabelleddatapetrinet.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.processmining.stochasticlabelleddatapetrinet.preprocess.OneHotEncoding;

public interface OneHotEncodingSerializer {

	void serialize(OneHotEncoding net, OutputStream os) throws IOException;

	OneHotEncoding deserialize(InputStream is)
			throws IOException, ClassNotFoundException;

}