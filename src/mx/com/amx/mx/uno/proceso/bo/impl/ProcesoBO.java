package mx.com.amx.mx.uno.proceso.bo.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import mx.com.amx.mx.uno.proceso.bo.IProcesoBO;
import mx.com.amx.mx.uno.proceso.dto.Categorias;
import mx.com.amx.mx.uno.proceso.dto.NoticiaRSSDTO;
import mx.com.amx.mx.uno.proceso.dto.Noticias;
import mx.com.amx.mx.uno.proceso.dto.ParametrosDTO;
import mx.com.amx.mx.uno.proceso.dto.RequestNotaDTO;
import mx.com.amx.mx.uno.proceso.dto.Secciones;
import mx.com.amx.mx.uno.proceso.utils.ObtenerProperties;
import mx.com.amx.mx.uno.proceso.utils.RSS;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

public class ProcesoBO implements IProcesoBO {
	
	private final Logger LOG = Logger.getLogger(this.getClass().getName());

	@Value( "${ambiente}" )
	String ambiente = "";	
	
	private  String URL_DAO = "";
	private final Properties props = new Properties();

	
	private RestTemplate restTemplate;
	HttpHeaders headers = new HttpHeaders();

	public ProcesoBO() {
		super();
		restTemplate = new RestTemplate();
		ClientHttpRequestFactory factory = restTemplate.getRequestFactory();
		
		if ( factory instanceof SimpleClientHttpRequestFactory) {
			((SimpleClientHttpRequestFactory) factory).setConnectTimeout( 120 * 1000 );
			((SimpleClientHttpRequestFactory) factory).setReadTimeout( 120 * 1000 );
		
		} else if ( factory instanceof HttpComponentsClientHttpRequestFactory) {
			((HttpComponentsClientHttpRequestFactory) factory).setReadTimeout( 120 * 1000);
			((HttpComponentsClientHttpRequestFactory) factory).setConnectTimeout( 120 * 1000);
		}
		
		restTemplate.setRequestFactory( factory );
		headers.setContentType(MediaType.APPLICATION_JSON);
	      
		try {
			props.load( this.getClass().getResourceAsStream( "/general.properties" ) );						
		} catch(Exception e) {
			LOG.error("[ConsumeWS::init]Error al iniciar y cargar arhivo de propiedades." + e.getMessage());
		}
		ambiente = props.getProperty("ambiente");
		URL_DAO = props.getProperty(ambiente+".ws.url.api.servicios");
	}
	
	public void procesoAutomatico() {
		LOG.info("************INI: GENERAR RSS *************");
		RSS getInfo = new RSS();
		try {
			getInfo.writeNewsML();
			getInfo.writeNewsHomePage();
			
			ObtenerProperties pro=new ObtenerProperties();
			ParametrosDTO parametros= pro.obtenerPropiedades();
			
			if(ambiente.equals("desarrollo")){
				getInfo.transfiereWebServer(parametros.getPathShell(), parametros.getRutaCarpeta(), parametros.getRutaDestino());
			}
			
		} catch (Exception e) {
			LOG.error("[procesoAutomatico] Error:" + e.getMessage());
		}
		
		LOG.info("************FIN: GENERAR RSS *************");
	}
	
	/**
	 * Metodo que obtiene las noticias seg�n la categoria.
	 * @param String idCategoria
	 * */
	public Noticias consultarNoticiasViralesNoTeLoPierdas( String viralNoTeLoPierdas ) throws Exception {
		
		Noticias noticias = new Noticias();
		try{
			String lstMETODO = "/consultarNoticiasViralesNoTeLoPierdas";
			String lstURL_WS = URL_DAO + lstMETODO;
			HttpEntity<String> entity = new HttpEntity<String>( viralNoTeLoPierdas, headers);
			noticias = restTemplate.postForObject(lstURL_WS , entity, Noticias.class);
						
		}catch(Exception e){
			LOG.error("Error consultarNoticiasViralesNoTeLoPierdas [BO]: ",e);
		}
		return noticias;		
	}
	
	/**
	 * Metodo que obtiene las noticias seg�n la categoria.
	 * @param String idCategoria
	 * */
	public Noticias consultarNoticiasMagazine( String idMagazine ) throws Exception {
		
		Noticias noticias = new Noticias();
		try{
			String lstMETODO = "/consultarNoticiasMagazine";
			String lstURL_WS = URL_DAO + lstMETODO;
			HttpEntity<String> entity = new HttpEntity<String>( idMagazine, headers);
			noticias = restTemplate.postForObject(lstURL_WS , entity, Noticias.class);
		
		}catch(Exception e){
			LOG.error("Error consultarNoticiasMagazine [BO]: ",e);
		}
		return noticias;		
	}
	
	/**
	 * Metodo que obtiene las noticias seg�n la categoria.
	 * @param String idCategoria
	 * */
	public Noticias consultarNoticiasHome(ParametrosDTO parametrosDTO) throws Exception {
		
		Noticias noticias = new Noticias();
		try{

			Noticias noticiasTemp = new Noticias();
			String homeNombre = "";
			
			//Obtiene la lista de parametros de configuracion por grupo de nota
			String[] listHomeConfig = parametrosDTO.getHomeRSSConfig().replaceAll("\\[", "").split("\\]");
			final String metodoGetNotaMagazine = "/getNotaMagazine";
			final String metodoGetNota = "/getNota";
			final String homeTipoMagazine = "magazine";
			final String homeTipoNota = "nota";
			final String homeNombreMagazine = parametrosDTO.getId_magazine_home();
			RequestNotaDTO requestNotaDTO = new RequestNotaDTO();
			
			//LOG.debug("parametrosDTO.getHomeRSSConfig():" + parametrosDTO.getHomeRSSConfig());
			
			List<NoticiaRSSDTO> listNoticiasHome = new ArrayList<NoticiaRSSDTO>();
			List<NoticiaRSSDTO> listNoticiasHomeTester = new ArrayList<NoticiaRSSDTO>();
			List<NoticiaRSSDTO> listNoticiasHomeIni = new ArrayList<NoticiaRSSDTO>();
			List<NoticiaRSSDTO> listNoticiasHomeFin = new ArrayList<NoticiaRSSDTO>();
			List<NoticiaRSSDTO> listNoticiasHomeNota = new ArrayList<NoticiaRSSDTO>();
						
			for(String homesConfig:listHomeConfig){
				
				String[] homeConfig = homesConfig.split("\\|");
				
				if(homeConfig.length > 2){
					homeNombre = homeConfig[0];//ej:home|video-viral|investigaciones-especiales
					String homeTipo = homeConfig[1];//ej:magazine|nota
					String[] homeParametros = homeConfig[2].split(",");//ej:magazine-home-2,na,na,na,na,0|na,na,na,na,FI_BAN_VIDEO_VIRAL,3|na,noticia,noticias,entretenimiento,na,3
					int numRegistrosAgregados = 0;
					int numRegistrosHomeIni = 0;
					
					if(homeParametros.length > 6){
						//settea parametros de request utilizados en dao de ws
						requestNotaDTO.setIdMagazine(homeParametros[0]);//ej:magazine-home-2
						requestNotaDTO.setIdTipoSeccion(homeParametros[1]);//ej:noticia
						requestNotaDTO.setIdSeccion(homeParametros[2]);//ej:noticias
						requestNotaDTO.setIdCategoria(homeParametros[3]);//ej:entretenimiento
						requestNotaDTO.setNomBandera(homeParametros[4]);//ej:FI_BAN_VIDEO_VIRAL
						requestNotaDTO.setNumRegistros(homeParametros[5] != null ? Integer.parseInt(homeParametros[5]) : 0);//ej:3
						requestNotaDTO.setNumRegistrosSubstring(homeParametros[6] != null ? Integer.parseInt(homeParametros[6]) : 0);//ej:0
						
						//Funciona para optimizar ejecucion de query al reducir numero de registros a recuperar
						if(listNoticiasHomeTester != null && !listNoticiasHomeTester.isEmpty()){
							requestNotaDTO.setNumRegistrosFetch(requestNotaDTO.getNumRegistros()+listNoticiasHomeTester.size());
						}
						else{
							requestNotaDTO.setNumRegistrosFetch(requestNotaDTO.getNumRegistros());
						}
						
						//LOG.debug("homeNombre:" + homeNombre + " NumRegistrosFetch" + requestNotaDTO.getNumRegistrosFetch());
					}
					
					HttpEntity<RequestNotaDTO> entity = new HttpEntity<RequestNotaDTO>(requestNotaDTO, headers);
					
					//listado de notas utilizando tabla magazine
					if(homeTipo.equalsIgnoreCase(homeTipoMagazine)){
						LOG.debug("metodo ws:" + URL_DAO + metodoGetNotaMagazine + " tipo:" + homeNombre);
						noticiasTemp = restTemplate.postForObject(URL_DAO + metodoGetNotaMagazine, entity, Noticias.class);
					}
					//listado de notas utilizando solo tabla nota
					else if(homeTipo.equalsIgnoreCase(homeTipoNota)){
						LOG.debug("metodo ws:" + URL_DAO + metodoGetNota + " tipo:" + homeNombre);
						noticiasTemp = restTemplate.postForObject(URL_DAO + metodoGetNota, entity, Noticias.class);
					}
					
					//loop notas obtenidas por ws
					if(noticiasTemp.getNoticiasLst() != null && !noticiasTemp.getNoticiasLst().isEmpty()){
						for(NoticiaRSSDTO noticiaWS : noticiasTemp.getNoticiasLst()){
							int banderaNotaExistente = 0;
							
							//loop notas agregadas y que se mostraran en rss
							for(NoticiaRSSDTO noticiaHome : listNoticiasHomeTester){
								
								//si nota de ws igual a nota agregada previamente, entonces break (indica que ya existe en la lista)
								if(noticiaWS.getFC_ID_CONTENIDO().equals(noticiaHome.getFC_ID_CONTENIDO())){
									banderaNotaExistente++;
									break;
								}
							}
							
							//si numero de registros agregados menor a num de registros requeridos
							if(numRegistrosAgregados<requestNotaDTO.getNumRegistros()){
								//si la nota no se encuentra en la lista de rss
								
								if(banderaNotaExistente==0){
									//se agrega nota
									listNoticiasHomeTester.add(noticiaWS);
									//aumento de registros agregados
									
									//Tratamiento especial magazine home
									if(requestNotaDTO.getIdMagazine().equals(homeNombreMagazine)){
										//si los registros de notas de magazine para la cabecera estan en el rango de notas a cargar
										if(numRegistrosHomeIni<requestNotaDTO.getNumRegistrosSubstring()){
											listNoticiasHomeIni.add(noticiaWS);
											numRegistrosHomeIni++;
										}
										//si no coloca en la lista de notas de magazine para footer
										else{
											listNoticiasHomeFin.add(noticiaWS);
										}
									}
									//cuando la nota no es de magazine y se encontrara entre la cabecera y el footer (en medio)
									else{
										listNoticiasHomeNota.add(noticiaWS);
									}
									
									
									numRegistrosAgregados++;
								}
							}
							//si no break
							else{
								break;
							}
						}
						
						
					}
					else{
						LOG.debug(homeNombre + " no tiene notas");
					}
					
				}
				
			}
			

			//[INI] Carga de notas finales
			//notas magazine iniciales
			if(listNoticiasHomeIni != null && !listNoticiasHomeIni.isEmpty()){
				for(NoticiaRSSDTO notaHomeIni: listNoticiasHomeIni){
					listNoticiasHome.add(notaHomeIni);
					//LOG.debug("notaHomeIni:" + notaHomeIni.getFcLink() + " id:" + notaHomeIni.getFC_ID_CONTENIDO() + " categoria:" +  notaHomeIni.getFC_ID_CATEGORIA() + " titulo:" + notaHomeIni.getFC_TITULO());
				}
			}
			//notas tabla nota
			if(listNoticiasHomeNota != null && !listNoticiasHomeNota.isEmpty()){
				for(NoticiaRSSDTO notaHomeNota: listNoticiasHomeNota){
					listNoticiasHome.add(notaHomeNota);
					//LOG.debug("notaHomeNota:" + notaHomeNota.getFcLink() + " id:" + notaHomeNota.getFC_ID_CONTENIDO() + " categoria:" +  notaHomeNota.getFC_ID_CATEGORIA() + " titulo:" + notaHomeNota.getFC_TITULO());
				}
			}
			//notas magazine finales
			if(listNoticiasHomeFin != null && !listNoticiasHomeFin.isEmpty()){
				for(NoticiaRSSDTO notaHomeFin: listNoticiasHomeFin){
					listNoticiasHome.add(notaHomeFin);
					//LOG.debug("notaHomeFin:" + notaHomeFin.getFcLink() + " id:" + notaHomeFin.getFC_ID_CONTENIDO() + " categoria:" +  notaHomeFin.getFC_ID_CATEGORIA() + " titulo:" + notaHomeFin.getFC_TITULO());
				}
			}
			//[FIN] Carga de notas finales
			
			if(listNoticiasHome == null || listNoticiasHome.isEmpty()){
				LOG.debug("Lista notas home.xml vacia");
			}
			else{
				LOG.debug("Lista notas home.xml:" + listNoticiasHome.size());
			}
			
			/*for(NoticiaRSSDTO noticiaFinal : listNoticiasHome){
				LOG.debug("noticiaFinal:" + homeNombre + noticiaFinal.getFcLink() + " id:" + noticiaFinal.getFC_ID_CONTENIDO() + " categoria:" +  noticiaFinal.getFC_ID_CATEGORIA() + " titulo:" + noticiaFinal.getFC_TITULO() + " link:" + noticiaFinal.getFcLink());
			}*/
			
			noticias.setNoticiasLst(listNoticiasHome);
			
		}catch(Exception e){
			LOG.error("Error consultarNoticiasHome [BO]: ",e);
		}
		return noticias;		
	}
	/**
	 * Metodo que obtiene las noticias seg�n la categoria.
	 * @param String idCategoria
	 * */
	public Noticias consultarNoticias( String idCategoria ) throws Exception {
		
		Noticias noticias = new Noticias();
		try{
			String lstMETODO = "/consultarNoticias";
			String lstURL_WS = URL_DAO + lstMETODO;
			
			HttpEntity<String> entity = new HttpEntity<String>( idCategoria, headers);
			noticias = restTemplate.postForObject(lstURL_WS , entity, Noticias.class);
						
		}catch(Exception e){
			LOG.error("Error consultarNoticias [BO]: ",e);
		}
		return noticias;		
	}
	
	/**
	 * Metodo que obtiene las noticias seg�n la secci�n.
	 * @param String idSeccion
	 * */
	public Noticias consultarUltimasPorSeccion( String idSeccion ) throws Exception {
		
		Noticias noticias = new Noticias();
		try{
			String lstMETODO = "/consultarUltimasPorSeccion";
			String lstURL_WS = URL_DAO + lstMETODO;
						
			HttpEntity<String> entity = new HttpEntity<String>( idSeccion, headers);
			noticias = restTemplate.postForObject(lstURL_WS , entity, Noticias.class);
			
		}catch(Exception e){
			LOG.error("Error consultarUltimasPorSeccion [BO]: ",e);
		}
		return noticias;		
	}
	
	/**
	 * Metodo que obtiene las noticias seg�n el tipo de secci�n.
	 * @param String idSeccion
	 * */
	public Noticias consultarUltimasPorTipoSeccion( String idSeccion ) throws Exception {
		
		Noticias noticias = new Noticias();
		try{
			String lstMETODO = "/consultarUltimasPorTipoSeccion";
			String lstURL_WS = URL_DAO + lstMETODO;
						
			HttpEntity<String> entity = new HttpEntity<String>( idSeccion, headers);
			noticias = restTemplate.postForObject(lstURL_WS , entity, Noticias.class);
			
		}catch(Exception e){
			LOG.error("Error consultarUltimasPorTipoSeccion [BO]: ",e);
		}
		return noticias;		
	}
	
	/**
	 * Metodo que obtiene las categorias.
	 * */
	public Categorias getCategorias( ) throws Exception {
		
		Categorias categorias = new Categorias();
		try{
			String lstMETODO = "/getCategorias";
			String lstURL_WS = URL_DAO + lstMETODO;
			HttpEntity<Integer> entity = new HttpEntity<Integer>( headers);
			categorias = restTemplate.postForObject(lstURL_WS , entity, Categorias.class);
						
		}catch(Exception e){
			LOG.error("Error getCategorias [BO]: ",e);
		}
		return categorias;		
	}
	
	/**
	 * Metodo que obtiene las secciones.
	 * */
	public Secciones getSecciones( ) throws Exception {
		
		Secciones secciones = new Secciones();
		try{
			String lstMETODO = "/getSecciones";
			String lstURL_WS = URL_DAO + lstMETODO;
		
			HttpEntity<Integer> entity = new HttpEntity<Integer>( headers);
			secciones = restTemplate.postForObject(lstURL_WS , entity, Secciones.class);
						
		}catch(Exception e){
			LOG.error("Error getSecciones [BO]: ",e);
			secciones.setSeccionesLst(null);
		}
		return secciones;		
	}
	
	/**
	 * Metodo que obtiene los tipos secciones.
	 * */
	public Secciones getTipoSecciones( ) throws Exception {
		
		Secciones secciones = new Secciones();
		try{
			String lstMETODO = "/getTipoSecciones";
			String lstURL_WS = URL_DAO + lstMETODO;
			HttpEntity<Integer> entity = new HttpEntity<Integer>( headers);
			secciones = restTemplate.postForObject(lstURL_WS , entity, Secciones.class);
						
		}catch(Exception e){
			LOG.error("Error getTipoSecciones [BO]: ",e);
			secciones.setSeccionesLst(null);
		}
		return secciones;		
	}
	
}