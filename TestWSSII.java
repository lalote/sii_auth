package cl.covepa;

import java.rmi.RemoteException;

public class TestWSSII {	
	
	public static void main(String[] args) {	
				
		try {
			RegistroReclamoDteServiceEndpointService reclamoService = new RegistroReclamoDteServiceEndpointServiceLocator();			
			RegistroReclamoDteService ws = reclamoService.getRegistroReclamoDteServicePort();			
			((RegistroReclamoDteServicePortBindingStub)ws).setMaintainSession(true);
			((RegistroReclamoDteServicePortBindingStub)ws)._setProperty(HTTPConstants.HEADER_COOKIE, "TOKEN=W4FGSUD3PD54K");

        	String fecha =  ws.consultarFechaRecepcionSii("rut-emisor","digito-verificado","cod-doc-sii","nro-tributario");
        	 
        	System.out.println("consultarFechaRecepcionSii : "+fecha);				

						
		} catch (ServiceException e) {
			e.printStackTrace();
		} catch (RemoteException e) {
			e.printStackTrace();
		}			

	}

}

