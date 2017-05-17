package cl.covepa;

import java.io.FileInputStream;

public class GetSIIToken {


	public static void main(String[] args) {
		CrSeedService crSeedService = new CrSeedServiceLocator();
		try {
			// se recupera la semilla de el SII
			CrSeed crSeed = crSeedService.getCrSeed();

			// generar XML con el numero de semilla y la estructura basica a firmar
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

			//SE CREA XML PARA FIRMAR, EN MEMORIA
			// elemento principal
			Document doc = docBuilder.newDocument();
			Element rootElement = doc.createElement("getToken");
			doc.appendChild(rootElement);

			// items
			Element item = doc.createElement("item");
			rootElement.appendChild(item);
			
			Element semilla = doc.createElement("Semilla");
			
			// se recupera el valor desde el elemento Semilla, enviado por el SII			
			String tagSemillaValue = getTagValue(crSeed.getSeed(), "SEMILLA");		
			
			System.out.println("VALOR SEMILLA WS SII : " + tagSemillaValue);
			
			// Semilla,se agrega el valor de la semilla devuelto por el WS de el SII
			semilla.appendChild(doc.createTextNode(tagSemillaValue));
			item.appendChild(semilla);

			// SE FIRMA EL DOCUMENTO XML
			String docFirmado = sign(doc);

			System.out.println("XML FIRMADO : " + docFirmado);

			// se conecta al WS
			GetTokenFromSeedService tokenService = new GetTokenFromSeedServiceLocator();
			String token = tokenService.getGetTokenFromSeed().getToken(docFirmado);

			System.out.println("TOKEN RECUPERADO : " + token);

		} catch (ParserConfigurationException | TransformerException | RemoteException | ServiceException
				| InstantiationException | IllegalAccessException | ClassNotFoundException | NoSuchAlgorithmException
				| InvalidAlgorithmParameterException | KeyException | FileNotFoundException | MarshalException
				| XMLSignatureException e) {
			e.printStackTrace();
		}

	}

	private static String sign(Document doc) throws InstantiationException, IllegalAccessException,
			ClassNotFoundException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, KeyException,
			MarshalException, XMLSignatureException, FileNotFoundException, TransformerException {


		XMLSignatureFactory fac = XMLSignatureFactory.getInstance("DOM");

		// Create a Reference to the enveloped document (in this case we are
		// signing the whole document, so a URI of "" signifies that) and
		// also specify the SHA1 digest algorithm and the ENVELOPED Transform.
		Reference ref = fac.newReference("", fac.newDigestMethod(DigestMethod.SHA1, null),
				Collections.singletonList(fac.newTransform(Transform.ENVELOPED, (TransformParameterSpec) null)), null,
				null);

		// Create the SignedInfo
		SignedInfo si = fac.newSignedInfo(
				fac.newCanonicalizationMethod(CanonicalizationMethod.INCLUSIVE, (C14NMethodParameterSpec) null),
				fac.newSignatureMethod(SignatureMethod.RSA_SHA1, null), Collections.singletonList(ref));

		// SE TRABAJA CON EL EL ALMACEN DE CERTIFICADOS DIGITALES
		KeyStore ks = null;
		X509Certificate cert = null;
		KeyStore.PrivateKeyEntry keyEntry = null;
		try {
			ks = KeyStore.getInstance("JKS");
			ks.load(new FileInputStream("src/cl/firma/sii_cert.jks"), "PASS_ALMACEN".toCharArray());
			keyEntry = (KeyStore.PrivateKeyEntry) ks.getEntry("ALIAS",new KeyStore.PasswordProtection("PASS_ALMACEN".toCharArray()));
			cert = (X509Certificate) keyEntry.getCertificate();
			
		} catch (KeyStoreException | CertificateException | IOException | UnrecoverableEntryException e) {
			e.printStackTrace();
		}
		
		//se crea unn KeyFactory
		KeyInfoFactory kif = fac.getKeyInfoFactory();
		//se usa la clave publica para firmar
		KeyValue kv = kif.newKeyValue(cert.getPublicKey());		

		// Create the KeyInfo containing the X509Data.
		List<X509Certificate> x509Content = new ArrayList<X509Certificate>();
		x509Content.add(cert);
		X509Data xd = kif.newX509Data(x509Content);

		// en una lista se agrega el KeyValue y el certificado X509Data a el KEYINFO
		List xml_tag_list = new ArrayList();
		xml_tag_list.add(kv); //TAG KeyValue
		xml_tag_list.add(xd); //TAG X509Data
		
		KeyInfo ki = kif.newKeyInfo(xml_tag_list);	
		
		// Create a DOMSignContext and specify the RSA PrivateKey and
		// location of the resulting XMLSignature's parent element
		DOMSignContext dsc = new DOMSignContext(keyEntry.getPrivateKey(), doc.getDocumentElement());

		// Create the XMLSignature (but don't sign it yet)
		XMLSignature signature = fac.newXMLSignature(si, ki);

		// Marshal, generate (and sign) the enveloped signature
		signature.sign(dsc);

		TransformerFactory tf = TransformerFactory.newInstance();
		Transformer trans = tf.newTransformer();
		trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");

		StringWriter writer = new StringWriter();
		trans.transform(new DOMSource(doc), new StreamResult(writer));

		return "<?xml version=\"1.0\"?>" + writer.getBuffer().toString().trim();
	}

	private static String getTagValue(String xml, String tagName) {
		return xml.split("<" + tagName + ">")[1].split("</" + tagName + ">")[0];
	}

}

