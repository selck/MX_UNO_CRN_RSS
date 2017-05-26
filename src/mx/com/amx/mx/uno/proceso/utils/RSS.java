package mx.com.amx.mx.uno.proceso.utils;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import mx.com.amx.mx.uno.proceso.bo.impl.ProcesoBO;
import mx.com.amx.mx.uno.proceso.dto.CategoriaDTO;
import mx.com.amx.mx.uno.proceso.dto.NoticiaRSSDTO;
import mx.com.amx.mx.uno.proceso.dto.ParametrosDTO;
import mx.com.amx.mx.uno.proceso.dto.SeccionDTO;
import mx.com.amx.mx.uno.proceso.dto.TipoSeccionDTO;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.w3c.dom.Attr;

public class RSS {
	
	private static final Logger log = Logger.getLogger(RSS.class);
	
	private ParametrosDTO parametros;
	private List<NoticiaRSSDTO> rssList2;

	private ProcesoBO procesoBO;
	
	public RSS(){
		ObtenerProperties props = new ObtenerProperties();
		parametros = props.obtenerPropiedades();
	}
	 
	public static String eliminaEspacios(String cad) {
		String cadena = "";
		cad = cad.trim();
		cadena = cad.replaceAll("\\s+", " ");
		cadena = cad.replaceAll("\t", "");
		cadena = cad.replaceAll("\t", "");
		cadena = cad.replaceAll("\n", "");
		cadena = cad.replace("^\\s+", "");
		cadena = cad.replace("\\s+$", "");
		return cadena.trim();
	}
	
	public static String filter(String str) {
		StringBuilder filtered = new StringBuilder(str.length());
		for (int i = 0; i < str.length(); i++) {
			char current = str.charAt(i);
			if (current == '�') {
				filtered.append("");
			} else{
				filtered.append(current);
			}
		}
		return filtered.toString();
	}

	public static String capitalizeString(String cad){
		String auxi="";
		try {
			auxi = cad.substring(0, 1).toUpperCase() + cad.substring(1);
		} catch (Exception e) {
			return cad;
		}
		return auxi;
	}
	
	public static void main(String [] args){
		try {
			String fecha2="2016-12-09 13:32:46.0";
			String fecha="2015-09-29 15:30:19.348";
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS");
			Date f=formatter.parse(fecha);
			
			
			
			SimpleDateFormat formatter2 = new SimpleDateFormat("");
			TimeZone tz = TimeZone.getTimeZone("America/Mexico_City");
	        DateFormat df = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z");
			df.setTimeZone(tz);

			String fechaS=df.format(f);
			
			System.out.println(fechaS);
			
			
	        SimpleDateFormat df2 = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z");
			df2.setTimeZone(tz);
			Date date = new Date();
			
			System.out.println(df2.format(date));
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	private void generarXMLHomePage(String stNombreArchivo, String tipo){
		DOMSource sourceRet  = new DOMSource();
		try {
			rssList = consultarNoticiasHome(); 
			if ( rssList != null && rssList.size() > 0 ){

				DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

				SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS");
				org.w3c.dom.Document docXML = docBuilder.newDocument();

				org.w3c.dom.Element rootElementP = docXML.createElement("rss");
				
				Attr attr_xmlns_dc = docXML.createAttribute("xmlns:dc");
				attr_xmlns_dc.setValue( "http://purl.org/dc/elements/1.1/" );
				Attr attr_xmlns_media = docXML.createAttribute("xmlns:media");
				attr_xmlns_media.setValue( "http://search.yahoo.com/mrss/" );
				Attr attr_xmlns_atom = docXML.createAttribute("xmlns:atom");
				attr_xmlns_atom.setValue( "http://www.w3.org/2005/Atom" );
				Attr attr_version = docXML.createAttribute("version");
				attr_version.setValue( "2.0" );
				
				rootElementP.setAttributeNode(attr_xmlns_dc);
				rootElementP.setAttributeNode(attr_xmlns_media);
				rootElementP.setAttributeNode(attr_xmlns_atom);
				rootElementP.setAttributeNode(attr_version);
				docXML.appendChild(rootElementP);

				org.w3c.dom.Element rootElement = docXML.createElement("channel");
				rootElementP.appendChild(rootElement);

				org.w3c.dom.Element titleP = docXML.createElement("title");
				titleP.appendChild(docXML.createTextNode("Uno TV"));
				rootElement.appendChild(titleP);

				org.w3c.dom.Element linkP = docXML.createElement("link");
				linkP.appendChild(docXML.createTextNode(parametros.getDominio()));
				rootElement.appendChild(linkP);
				
				org.w3c.dom.Element atom_link = docXML.createElement("atom:link");
				Attr attr_rel = docXML.createAttribute("rel");
				attr_rel.setValue( "self" );
				Attr attr_type = docXML.createAttribute("type");
				attr_type.setValue( "application/rss+xml" );
				Attr attr_href = docXML.createAttribute("href");
				attr_href.setValue( parametros.getDominio()+"/rss/homePage.xml" );
				
				atom_link.setAttributeNode(attr_rel);
				atom_link.setAttributeNode(attr_type);
				atom_link.setAttributeNode(attr_href);
				
				//atom_link.appendChild(docXML.createCDATASection());
				rootElement.appendChild(atom_link);

				org.w3c.dom.Element descriptionP = docXML.createElement("description");
				//org.w3c.dom.Element descriptionP = docXML.createElement("og:description");
				descriptionP.appendChild(docXML.createTextNode("Portal de noticias"));
				rootElement.appendChild(descriptionP);

				org.w3c.dom.Element languageP = docXML.createElement("language");
				languageP.appendChild(docXML.createTextNode("es-MX"));
				rootElement.appendChild(languageP);
				
				org.w3c.dom.Element copyright = docXML.createElement("copyright");
				copyright.appendChild(docXML.createTextNode("Derechos Reservados 2017 Publicidad y Contenido Editorial S.A. de C.V"));
				rootElement.appendChild(copyright);
				
				org.w3c.dom.Element lastBuildDate = docXML.createElement("lastBuildDate");
				TimeZone tz = TimeZone.getTimeZone("America/Mexico_City");
		        //DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
		        SimpleDateFormat df = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z");
				df.setTimeZone(tz);
				Date date = new Date();
				
				lastBuildDate.appendChild(docXML.createTextNode(df.format(date)));
				rootElement.appendChild(lastBuildDate);
				
				org.w3c.dom.Element image = docXML.createElement("image");
				org.w3c.dom.Element title = docXML.createElement("title");
				title.appendChild(docXML.createTextNode("Uno TV"));
				org.w3c.dom.Element url = docXML.createElement("url");
				url.appendChild(docXML.createTextNode("http://www.unotv.com/utils/img/logo-unotv-dark.png"));
				org.w3c.dom.Element link = docXML.createElement("link");
				link.appendChild(docXML.createTextNode(parametros.getDominio()));
				
				image.appendChild(title);
				image.appendChild(url);
				image.appendChild(link);
				rootElement.appendChild(image);
				
				for ( int i=0; i < rssList.size() ; i++ ){				
					
					StringBuffer sb = new StringBuffer();

					org.jdom.Element question = new org.jdom.Element("item");

					org.w3c.dom.Element noticiaXML = docXML.createElement("item");
					rootElement.appendChild(noticiaXML);

					sb.delete(0, sb.length());

					org.jdom.Element item = new org.jdom.Element("item");

					org.w3c.dom.Element title_item = docXML.createElement("title");
					title_item.appendChild(docXML.createTextNode(rssList.get(i).getFC_TITULO()));
					/*
					//Section
					org.w3c.dom.Element section = docXML.createElement("section");
					section.appendChild(docXML.createCDATASection(rssList.get(i).getFC_SECCION()== null?"":rssList.get(i).getFC_SECCION()));
					//category
					org.w3c.dom.Element category = docXML.createElement("category");
					category.appendChild(docXML.createCDATASection(rssList.get(i).getFC_CATEGORIA()==null?"":rssList.get(i).getFC_CATEGORIA()));
					//category description
					org.w3c.dom.Element category_description = docXML.createElement("category-description");
					category_description.appendChild(docXML.createCDATASection(rssList.get(i).getFC_DESCRIPCION_CATEGORIA()==null?"":rssList.get(i).getFC_DESCRIPCION_CATEGORIA()));
					*/

					org.w3c.dom.Element link_item = docXML.createElement("link");
					// LINK
					String link_lista = rssList.get(i).getFcLink();
					
					if(link_lista.indexOf("http") != -1){
						link_item.appendChild(docXML.createTextNode(link_lista));
					}else if(tipo.equalsIgnoreCase("home")){
						link_item.appendChild(docXML.createTextNode( parametros.getDominio() + "/" + link_lista + "/" ));
					}else{
						link_item.appendChild(docXML.createTextNode( parametros.getDominio() + "/" + link_lista + "/" ));
					}
					
					org.w3c.dom.Element guid_item = docXML.createElement("guid");
					guid_item.setAttribute("isPermaLink", "true");
					guid_item.appendChild(docXML.createTextNode(parametros.getDominio() + "/" +link_lista+"/"));
					
					org.w3c.dom.Element atom_link_item = docXML.createElement("atom:link");
					atom_link_item.setAttribute("rel", "standout");
					atom_link_item.setAttribute("href", parametros.getDominio()+"/"+link_lista+"/");
			
					org.w3c.dom.Element description_item = docXML.createElement("description");
					description_item.appendChild(docXML.createTextNode(rssList.get(i).getFC_DESCRIPCION()));
					
					org.w3c.dom.Element pubDate_item = docXML.createElement("pubDate");
					//log.info("rssList.get(i).getFD_FECHA_PUBLICACION(): "+rssList.get(i).getFD_FECHA_PUBLICACION());
					Date f=formatter.parse(rssList.get(i).getFD_FECHA_PUBLICACION());
					pubDate_item.appendChild(docXML.createTextNode(df.format(f))); 
					//
					/*org.w3c.dom.Element guid = docXML.createElement("guid");
					// GUID
					String guidClean = linkseccion;
					String guidUrl = rssList.get(i).getFcLink();
					if(guidUrl.indexOf("http") != -1){
						guid.appendChild(docXML.createCDATASection(guidUrl));
					}else if(tipo.equalsIgnoreCase("home")){
						guid.appendChild(docXML.createCDATASection( parametros.getDominio() + "/" + guidUrl + guidClean + "/" + utmHome));
					}
					else{
						guid.appendChild(docXML.createCDATASection( parametros.getDominio() + "/" + guidUrl + guidClean + "/" ));
					}*/
					//
					noticiaXML.appendChild(title_item);
					noticiaXML.appendChild(link_item);
					noticiaXML.appendChild(guid_item);
					//noticiaXML.appendChild(guid_item);
					noticiaXML.appendChild(atom_link_item);
					noticiaXML.appendChild(description_item);
					noticiaXML.appendChild(pubDate_item);
					
					question.addContent( item );
				}

				TransformerFactory transformerFactory = TransformerFactory.newInstance();
				Transformer transformer = transformerFactory.newTransformer();
				transformer.setOutputProperty(OutputKeys.INDENT, "yes");
				DOMSource source = new DOMSource(docXML);

				sourceRet = source;

				//File f = new File (parametros.getRutaCarpeta() + stNombreArchivo );
				//File f = new File ("C:/pruebas/rss_unotv/" + stNombreArchivo );
				File f = new File ("/var/dev-repos/unotv/rss_home_page/" + stNombreArchivo );
				StreamResult result = new StreamResult( f );

				log.info(" Archivo generado en:" + f.getAbsoluteFile());
				transformer.transform(sourceRet, result);

			}
		} catch (Exception e) {
			log.info("Error generarXMLHomePage: ",e);
		}
	}
	private void generarXML(String stNombreArchivo, String stNombreSeccion, String tipo){
		String utmHome="?utm_campaign=start-app&utm_medium=link&utm_source=feed&ns_mchannel=link&ns_source=feed&ns_campaign=start-app&ns_linkname=cmi";
		DOMSource sourceRet  = new DOMSource();
		String linkseccion = "";
		try {
			if(tipo.equalsIgnoreCase("seccion")){
				rssList = consultarUltimasPorSeccion(stNombreSeccion);
			} else if(tipo.equalsIgnoreCase("tipoSeccion")){
				rssList = consultarUltimasPorTipoSeccion(stNombreSeccion); 
			}  else if(tipo.equalsIgnoreCase("virales")){
				rssList = consultarNoticiasViralesNoTeLoPierdas(tipo); 
			} else if(tipo.equalsIgnoreCase("no-te-lo-pierdas")){
				rssList = consultarNoticiasViralesNoTeLoPierdas(tipo);
			} else if(tipo.equalsIgnoreCase("magazine")){
				rssList = consultarNoticiasMagazine(stNombreSeccion);
			} else if(tipo.equalsIgnoreCase("home")){
				rssList = consultarNoticiasHome(); 
			}else{
				rssList = obtenerNoticias(stNombreSeccion); 
			}
		
			if ( rssList != null && rssList.size() > 0 ){

				DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

				//SimpleDateFormat fc = new SimpleDateFormat("MM/dd/yy HH:mm a");

				org.w3c.dom.Document docXML = docBuilder.newDocument();

				org.w3c.dom.Element rootElementP = docXML.createElement("rss");
				Attr attr = docXML.createAttribute("version");
				attr.setValue( "2.0" );
				rootElementP.setAttributeNode(attr);
				docXML.appendChild(rootElementP);

				org.w3c.dom.Element rootElement = docXML.createElement("channel");
				rootElementP.appendChild(rootElement);

				org.w3c.dom.Element titleP = docXML.createElement("title");
				//org.w3c.dom.Element titleP = docXML.createElement("og:title");
				//titleP.appendChild(docXML.createCDATASection("UnoNoticias - " + stNombreSeccion.replace("noticias-", "")));
				if(tipo.equalsIgnoreCase("home"))
					titleP.appendChild(docXML.createCDATASection("Home"));
				else
					titleP.appendChild(docXML.createCDATASection(rssList.get(0).getFC_TITULO_RSS()));
				
				rootElement.appendChild(titleP);

				org.w3c.dom.Element linkP = docXML.createElement("link");
				linkP.appendChild(docXML.createCDATASection(parametros.getDominio()));
				rootElement.appendChild(linkP);

				org.w3c.dom.Element descriptionP = docXML.createElement("description");
				//org.w3c.dom.Element descriptionP = docXML.createElement("og:description");
				descriptionP.appendChild(docXML.createCDATASection("Portal de noticias"));
				rootElement.appendChild(descriptionP);

				org.w3c.dom.Element languageP = docXML.createElement("language");
				languageP.appendChild(docXML.createCDATASection("es-MX"));
				rootElement.appendChild(languageP);

				for ( int i=0; i < rssList.size() ; i++ ){				
					
					StringBuffer sb = new StringBuffer();

					org.jdom.Element question = new org.jdom.Element("item");

					org.w3c.dom.Element noticiaXML = docXML.createElement("item");
					rootElement.appendChild(noticiaXML);

					sb.delete(0, sb.length());

					org.jdom.Element item = new org.jdom.Element("item");

					// TITLE
					org.w3c.dom.Element title = docXML.createElement("title");
					//org.w3c.dom.Element title = docXML.createElement("og:title");
					String titulo = rssList.get(i).getFC_TITULO();
					title.appendChild(docXML.createCDATASection(titulo));
					//Image
					//org.w3c.dom.Element image = docXML.createElement("image");
					//String imagen = rssList.get(i).getFC_IMAGEN_PRINCIPAL();
					//image.appendChild(docXML.createCDATASection(imagen));
					//Section
					org.w3c.dom.Element section = docXML.createElement("section");
					section.appendChild(docXML.createCDATASection(rssList.get(i).getFC_SECCION()== null?"":rssList.get(i).getFC_SECCION()));
					//category
					org.w3c.dom.Element category = docXML.createElement("category");
					category.appendChild(docXML.createCDATASection(rssList.get(i).getFC_CATEGORIA()==null?"":rssList.get(i).getFC_CATEGORIA()));
					//category description
					org.w3c.dom.Element category_description = docXML.createElement("category-description");
					category_description.appendChild(docXML.createCDATASection(rssList.get(i).getFC_DESCRIPCION_CATEGORIA()==null?"":rssList.get(i).getFC_DESCRIPCION_CATEGORIA()));
					//
					org.w3c.dom.Element url = docXML.createElement("link");
					// LINK
					String linkClean = linkseccion;
					String link = rssList.get(i).getFcLink();
					
					if(link.indexOf("http") != -1){
						url.appendChild(docXML.createCDATASection(link));
					}else if(tipo.equalsIgnoreCase("home")){
						url.appendChild(docXML.createCDATASection( parametros.getDominio() + "/" + link + linkClean + "/" + utmHome));
					}else{
						url.appendChild(docXML.createCDATASection( parametros.getDominio() + "/" + link + linkClean + "/"  ));
					}
					
					//
					// PUBDATE
					org.w3c.dom.Element pubDate = docXML.createElement("pubDate");
					pubDate.appendChild(docXML.createCDATASection(rssList.get(i).getFD_FECHA_PUBLICACION()));
					
					// DESCRIPTION
					org.w3c.dom.Element description = docXML.createElement("description");
					String descripcion = rssList.get(i).getFC_DESCRIPCION();
					description.appendChild(docXML.createCDATASection(descripcion));
					//
					org.w3c.dom.Element guid = docXML.createElement("guid");
					// GUID
					String guidClean = linkseccion;
					String guidUrl = rssList.get(i).getFcLink();
					if(guidUrl.indexOf("http") != -1){
						guid.appendChild(docXML.createCDATASection(guidUrl));
					}else if(tipo.equalsIgnoreCase("home")){
						guid.appendChild(docXML.createCDATASection( parametros.getDominio() + "/" + guidUrl + guidClean + "/" + utmHome));
					}
					else{
						guid.appendChild(docXML.createCDATASection( parametros.getDominio() + "/" + guidUrl + guidClean + "/" ));
					}
					//
					noticiaXML.appendChild(title);
					if(tipo.equalsIgnoreCase("home")){
						noticiaXML.appendChild(section);
						noticiaXML.appendChild(category);
					}
					noticiaXML.appendChild(category_description);
					noticiaXML.appendChild(url);
					noticiaXML.appendChild(guid);
					noticiaXML.appendChild(pubDate);
					noticiaXML.appendChild(description);
					
					question.addContent( item );
				}

				TransformerFactory transformerFactory = TransformerFactory.newInstance();
				Transformer transformer = transformerFactory.newTransformer();
				transformer.setOutputProperty(OutputKeys.INDENT, "yes");
				DOMSource source = new DOMSource(docXML);

				sourceRet = source;

				File f = new File (parametros.getRutaCarpeta() + stNombreArchivo );
				//File f = new File ("C:/rss/finales/" + stNombreArchivo );
				StreamResult result = new StreamResult( f );

				log.info(" Archivo  " + f.getAbsoluteFile());

				transformer.transform(sourceRet, result);

				//se comenta por el uso de carpetas compartidas en la nube.
//				transfiereWebServer(parametros.getPathShell(),parametros.getRutaCarpeta()+"*",parametros.getRutaDestino());

			} 
			// end if

		}catch (Exception e){
			log.info("Error " + e);
		}
	}

	/**
	 * @param local
	 * @param remote
	 * @return
	 */
	public boolean transfiereWebServer(String rutaShell, String pathLocal, String pathRemote) {
		boolean success = false;

		String comando = "";
		  
		if(pathLocal.equals("") && pathRemote.equals("")){
			  comando = rutaShell;
		} else{
			  comando = rutaShell + " " + pathLocal+ "* " + pathRemote;
		}
		
		log.info("Comando:  " + comando);
		try {
			Runtime r = Runtime.getRuntime();
			Process p = r.exec(comando);
		
			//Para validar la ejecuci�n del shell
	/*		String line;
			BufferedReader input = new BufferedReader(new InputStreamReader(r.exec(comando).getErrorStream()));
			LOG.debug("*****");
			while ((line = input.readLine()) != null) {
				LOG.debug(line);
			}
			input.close();
			LOG.debug("*****"); */
			
			success = true;
		} catch(Exception e) {
			success = false;
			log.error("Ocurrio un error al ejecutar el Shell " + comando + ": ", e);
		}
		return success;
	}
	
	public org.w3c.dom.Document writeNewsHomePage() {
		log.info(".: Ejecutandose writeNewsHomePage...");
		try{
			procesoBO = new ProcesoBO();
			log.info(":::: [INI] generamos archivos homePage ::::");
			generarXMLHomePage("homePage.xml", "home");
			log.info(":::: [FIN] generamos archivos homePage ::::");
		
		} catch ( Exception e ){
			log.error("Error writeNewsHomePage" + e.getCause());
		}
		
		return null;	
	}
	
	public org.w3c.dom.Document writeNewsML() {
		log.info(".: Ejecutandose...");
		try{
			procesoBO = new ProcesoBO();
			
			log.info(":::: [INI] generamos archivos por categorias ::::");
			List<CategoriaDTO> lst = procesoBO.getCategorias().getCategotiasLst();
			for(CategoriaDTO dto : lst){
				generarXML(dto.getFC_ID_CATEGORIA()+".xml", dto.getFC_ID_CATEGORIA(), "categoria");
			}
			log.info(":::: [FIN] generamos archivos por categorias ::::");
			
			log.info(":::: [INI] generamos archivos por seccion ::::");
			List<SeccionDTO> lstSecciones = procesoBO.getSecciones().getSeccionesLst();
			for(SeccionDTO dto : lstSecciones){
				generarXML(dto.getFC_FRIENDLY_URL()+"sec.xml", dto.getFC_ID_SECCION(), "seccion");
			}
			log.info(":::: [FIN] generamos archivos por seccion ::::");
			
			log.info(":::: [INI] generamos archivos por tipo seccion ::::");
			List<TipoSeccionDTO> lstTipoSecciones = procesoBO.getTipoSecciones().getTipoSeccionesLst();
			for(TipoSeccionDTO dto : lstTipoSecciones){
				generarXML(dto.getFC_FRIENDLY_URL()+".xml", dto.getFC_ID_TIPO_SECCION(), "tipoSeccion");
			}
			log.info(":::: [FIN] generamos archivos por tipo seccion ::::");
			
			log.info(":::: [INI] generamos archivos NoticiasViralesNoTeLoPierdas ::::");
				generarXML("virales.xml", "virales", "virales");
				generarXML("no-te-lo-pierdas.xml", "no-te-lo-pierdas", "no-te-lo-pierdas");
			log.info(":::: [FIN] generamos archivos NoticiasViralesNoTeLoPierdas ::::");
			
			log.info(":::: [INI] generamos archivos NoticiasMagazine ::::");
				generarXML("magazine.xml", parametros.getId_magazine_home(), "magazine");
			log.info(":::: [FIN] generamos archivos NoticiasMagazine ::::");
			
			log.info(":::: [INI] generamos archivos Home ::::");
				generarXML("home.xml", "", "home");
			log.info(":::: [FIN] generamos archivos Home ::::");
			
			log.info(":::: [INI] actualizamos monitoreo");
			EscribeArchivoMonitoreo.escribeArchivoMon(parametros);
			log.info(":::: [FIN] actualizamos monitoreo");
			
		} catch ( Exception e ){
			log.error("[ getInfoRSS ] Ocurrio un error al obtener informacion " + e.getCause());
		}
		
		return null;
	}
	
	/**
	 * @param idSeccion
	 * @return List<NoticiaRSSDTO>
	 * @throws Exception
	 */
	public List<NoticiaRSSDTO> consultarNoticiasMagazine(String idMagazine) throws Exception{
		procesoBO = new ProcesoBO();
		List<NoticiaRSSDTO> listaNoticias = procesoBO.consultarNoticiasMagazine(idMagazine).getNoticiasLst();
		return listaNoticias;
	}

	/**
	 * @param idSeccion
	 * @return List<NoticiaRSSDTO>
	 * @throws Exception
	 */
	public List<NoticiaRSSDTO> consultarNoticiasHome() throws Exception{
		procesoBO = new ProcesoBO();
		List<NoticiaRSSDTO> listaNoticias = procesoBO.consultarNoticiasHome(parametros).getNoticiasLst();
		return listaNoticias;
	}
	
	/**
	 * @param viralNoTeLoPierdas
	 * @return List<NoticiaRSSDTO>
	 * @throws Exception
	 */
	public List<NoticiaRSSDTO> consultarNoticiasViralesNoTeLoPierdas(String viralNoTeLoPierdas) throws Exception{
		procesoBO = new ProcesoBO();
		List<NoticiaRSSDTO> listaNoticias = procesoBO.consultarNoticiasViralesNoTeLoPierdas(viralNoTeLoPierdas).getNoticiasLst();
		return listaNoticias;
	}
	/**
	 * @param idCategoria
	 * @return
	 * @throws Exception
	 */
	public List<NoticiaRSSDTO> obtenerNoticias(String idCategoria) throws Exception{
		procesoBO = new ProcesoBO();
		List<NoticiaRSSDTO> listaNoticias = procesoBO.consultarNoticias(idCategoria).getNoticiasLst();;
		return listaNoticias;
	}
	
	/**
	 * @param idSeccion
	 * @return List<NoticiaRSSDTO>
	 * @throws Exception
	 */
	public List<NoticiaRSSDTO> consultarUltimasPorSeccion(String idSeccion) throws Exception{
		procesoBO = new ProcesoBO();
		List<NoticiaRSSDTO> listaNoticias = procesoBO.consultarUltimasPorSeccion(idSeccion).getNoticiasLst();
		return listaNoticias;
	}
	
	/**
	 * @param idSeccion
	 * @return List<NoticiaRSSDTO>
	 * @throws Exception
	 */
	public List<NoticiaRSSDTO> consultarUltimasPorTipoSeccion(String idSeccion) throws Exception{
		procesoBO = new ProcesoBO();
		List<NoticiaRSSDTO> listaNoticias = procesoBO.consultarUltimasPorTipoSeccion(idSeccion).getNoticiasLst();
		return listaNoticias;
	}
	
	@Autowired
	public void setProcesoBO(ProcesoBO procesoBO) {
		this.procesoBO = procesoBO;
	}

	public ProcesoBO getProcesoBO() {
		return procesoBO;
	}
	
}