RESCATAR SEMILLA Y FIRMAR XML PARA ENTREGA DE TOKEN

En Eclipse con Axis se llama al Web Service de el SII para pedir la SEMILLA 

https://palena.sii.cl/DTEWS/CrSeed.jws?WSDL

Luego de generar los Stub, se usa la clase GetSIIToken para generar el XML firmado 
con el Certificado Digital

y se llama al Web Service (Generar el Stub antes de usar la clase)

https://palena.sii.cl/DTEWS/GetTokenFromSeed.jws?WSDL

para la entrega de el TOKEN que indica que estamos autorizados a usar los Web Service del SII

Si todo funciona bien debiese devolver un XML como el siguiente (de aqui solo rescatamos TOKEN) :

<?xml version="1.0" encoding="UTF-8"?><SII:RESPUESTA xmlns:SII="http://www.sii.cl/XMLSchema"><SII:RESP_BODY><TOKEN>WFSYRDEJWOAW0</TOKEN></SII:RESP_BODY><SII:RESP_HDR><ESTADO>00</ESTADO><GLOSA>Token Creado</GLOSA></SII:RESP_HDR></SII:RESPUESTA>


EJEMPLO DE LLAMADO A WEBSERVICE CON TOKEN AGREGADO A LA CABECERA

Una vez generado el token, cambiamos el valor en la clase TestWSSII y llamamos al webservice

https://ws2.sii.cl/WSREGISTRORECLAMODTECERT/registroreclamodteservice?wsdl

Si todo funciona bien, debiese responder con la fecha de recepcion de el documento ingresado.
Si no hay autorizacion un error 401 será enviado.



SI TIENES EL CERTIFICADO COMO .PFX

Es necesario pasar el archivo cifrado en SSL pfx a uno JKS con cifrado PKCS12, se realiza con este comando

keytool -importkeystore -srckeystore certificado.pfx -srcstoretype pkcs12  -destkeystore sii_cert.jks -deststoretype JKS


