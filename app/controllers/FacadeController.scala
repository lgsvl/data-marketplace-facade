package controllers

import java.net.URLEncoder
import javax.inject._
import play.Logger
import play.api._
import play.api.mvc._
import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.ws._
import play.api.libs.ws.ahc.AhcCurlRequestLogger
import scala.concurrent._

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class FacadeController @Inject()(cc: ControllerComponents, ws: WSClient, config: Configuration)(implicit ec: ExecutionContext) extends AbstractController(cc) {

  val chain_service_host = config.get[String]("chain_service_host")

  private def enrichAndProxyRequest(original: Request[_], url: String)(f: WSRequest => Future[WSResponse]): Future[Result] = {
    val proxyHeaders = original.headers.headers.filter(h => "authorization".equalsIgnoreCase(h._1))
    val request = ws.url(url)
      .addHttpHeaders("content-type" -> "application/json")
      .addHttpHeaders("accept" -> "application/json")
      .addHttpHeaders(proxyHeaders: _*)
      .withRequestFilter(AhcCurlRequestLogger())

      f(request).map { response =>
        Status(response.status)(response.body)
      }
  }

// Catalog API 

  def listCatalogEntries() = Action.async { request =>
    val url = s"${chain_service_host}api/queries/getDataContractTypes"

    println("Requesting catalog contents list")
    enrichAndProxyRequest(request, url)( _.get())
  }




  def addCatalogEntry() = Action(parse.json).async { request: Request[JsValue] =>
    val url = s"${chain_service_host}api/DataContractType"
    enrichAndProxyRequest(request, url)( _.post(request.body))
  }

  def lookupCatalogEntry(id: String) = Action.async { request =>
    val encodedId = URLEncoder.encode(id, "UTF-8")
    val url = s"${chain_service_host}api/DataContractType/$encodedId"
    enrichAndProxyRequest(request, url)( _.get())
  }

  def lookupCatalogEntriesByCategory(id: String) = Action.async { request =>
    val url = s"${chain_service_host}api/queries/getDataContractTypesByCategory"
    enrichAndProxyRequest(request, url){ws =>
      ws.addQueryStringParameters("categoryID" -> id)
        .get()
    }
  }

  def lookupCatalogEntriesByProvider(id: String) = Action.async { request =>
    val url = s"${chain_service_host}api/queries/getDataContractTypesByProvider"
    enrichAndProxyRequest(request, url){ws =>
      ws.addQueryStringParameters("providerID" -> id)
        .get()
    }
  }

  def lookupPopularCatalogEntries(size: String) = Action.async { request =>
    val url = s"${chain_service_host}api/queries/getPopularDataContractTypes"
    enrichAndProxyRequest(request, url){ws =>
      ws.addQueryStringParameters("size" -> size)
        .get()
    }
  }

  def lookupRecommendedCatalogEntry() = Action.async { request =>
    val url = s"${chain_service_host}api/queries/getRecommendedDataContractType"
    enrichAndProxyRequest(request, url){ _.get() }
  }

  def removeCatalogEntry(id: String) = Action.async { request =>
    val encodedId = URLEncoder.encode(id, "UTF-8")
    val url = s"${chain_service_host}api/DataContractType/$encodedId"
    enrichAndProxyRequest(request, url)( _.delete())
  }

  def updateCatalogEntry(id: String) = Action(parse.json).async { request: Request[JsValue] =>
    val encodedId = URLEncoder.encode(id, "UTF-8")
    val url = s"${chain_service_host}api/DataContractType/$encodedId"
    enrichAndProxyRequest(request, url)( _.put(request.body))
  }

// Category API 

  def listCategoryEntries() = Action.async { request =>
    val url = s"${chain_service_host}api/queries/getDataCategories"
    enrichAndProxyRequest(request, url)( _.get())
  }


  def addCategoryEntry() = Action(parse.json).async { request: Request[JsValue] =>
    val url = s"${chain_service_host}api/DataCategory"
    enrichAndProxyRequest(request, url)( _.post(request.body))
  }

  def lookupCategoryEntry(id: String) = Action.async { request =>
    val encodedId = URLEncoder.encode(id, "UTF-8")
    val url = s"${chain_service_host}api/DataCategory/$encodedId"
    enrichAndProxyRequest(request, url)( _.get())
  }

  def lookupPopularCategories(size: Long) = Action.async { request =>
    val url = s"${chain_service_host}api/queries/getPopularDataCategories"
    enrichAndProxyRequest(request, url){ws =>
      ws.addQueryStringParameters("size" -> size.toString())
        .get()
    }
  }

  def lookupCategoriesWithPagination(pageSize: Long, bookmark: Option[String]) = Action.async { request =>
    val url = s"${chain_service_host}api/queries/getDataCategoriesWithPagination"
    var queryParams = List("pageSize" -> pageSize.toString())
    bookmark.foreach { bm =>
      queryParams = ("bookmark" -> bm) :: queryParams
    }
    enrichAndProxyRequest(request, url){ws =>
      ws.addQueryStringParameters(queryParams: _*)
        .get()
    }
  }

  def removeCategoryEntry(id: String) = Action.async { request =>
    val encodedId = URLEncoder.encode(id, "UTF-8")
    val url = s"${chain_service_host}api/DataCategory/$encodedId"
    enrichAndProxyRequest(request, url)( _.delete())
  }

  def updateCategoryEntry(id: String) = Action(parse.json).async { request: Request[JsValue] =>
    val encodedId = URLEncoder.encode(id, "UTF-8")
    val url = s"${chain_service_host}api/DataCategory/$encodedId"
    enrichAndProxyRequest(request, url)( _.put(request.body))
  }



// Trade API (DataContract + Business)
  def listDataContractEntries() = Action.async { request =>
    val url = s"${chain_service_host}api/queries/getDataContracts"
    enrichAndProxyRequest(request, url){ _.get()}
  }


  def addDataContractProposal() = Action(parse.json).async { request: Request[JsValue] =>
    val url = s"${chain_service_host}api/SubmitDataContractProposal"
    enrichAndProxyRequest(request, url){ _.post(request.body)}
  }

  def lookupBusinessDataSetsPurchasedNotUploaded(id: String) = Action.async { request =>
    val url = s"${chain_service_host}api/queries/selectBusinessDataSetsPurchasedNotUploaded"
    enrichAndProxyRequest(request, url) { ws =>
      ws.addQueryStringParameters("consumerID" -> id)
        .get()
    }
  }

  def lookupBusinessDataSetsPurchasedDownloaded(id: String, today: String) = Action.async { request =>
    val url = s"${chain_service_host}api/queries/selectBusinessDataSetsPurchasedDownloaded"
    enrichAndProxyRequest(request, url){ ws =>
      ws.addQueryStringParameters("consumerID" -> id, "today" -> today)
        .get()
    }
  }

  def lookupBusinessDataSetsPurchasedUploadedNotDownloaded(id: String, today: String) = Action.async { request =>
    val url = s"${chain_service_host}api/queries/selectBusinessDataSetsPurchasedUploadedNotDownloaded"
    enrichAndProxyRequest(request, url){ ws =>
      ws.addQueryStringParameters("consumerID" -> id, "today" -> today)
        .get()
    }
  }

  def lookupBusinessDataSetsSoldAndDownloaded(id: String, today: String) = Action.async { request =>
    val url = s"${chain_service_host}api/queries/selectBusinessDataSetsSoldAndDownloaded"
    enrichAndProxyRequest(request, url){ws =>
      ws.addQueryStringParameters("providerID" -> id, "today" -> today)
        .get()
    }
  }

  def lookupBusinessDataSetsSoldShippedNotDownloaded(id: String, today: String) = Action.async { request =>
    val url = s"${chain_service_host}api/queries/selectBusinessDataSetsSoldShippedNotDownloaded"
    enrichAndProxyRequest(request, url){ws =>
      ws.addQueryStringParameters("providerID" -> id, "today" -> today)
        .get()
    }
  }

  def lookupBusinessDataSetsToUpload(id: String) = Action.async { request =>
    val url = s"${chain_service_host}api/queries/selectBusinessDataSetsToUpload"
    enrichAndProxyRequest(request, url){ws =>
      ws.addQueryStringParameters("providerID" -> id)
        .get()
    }
  }

  def lookupNumberOfBusinessDataSetsToUpload(id: String) = Action.async { request =>
    val url = s"${chain_service_host}api/queries/selectNumberOfBusinessDataSetsToUpload"
    enrichAndProxyRequest(request, url){ws =>
      ws.addQueryStringParameters("providerID" -> id)
        .get()
    }
  }

  def lookupBusinessDataSetsToUploadByCatalogEntry(id: String) = Action.async { request =>
    val url = s"${chain_service_host}api/queries/selectBusinessDataSetsToUploadByDataContractType"
    enrichAndProxyRequest(request, url){ws =>
      ws.addQueryStringParameters("dataContractType" -> id)
        .get()
    }
  }

  def lookupBusinessDataSetsByCatalogEntry(id: String) = Action.async { request =>
    val url = s"${chain_service_host}api/queries/selectBusinessDataSetsByDataContractType"
    enrichAndProxyRequest(request, url){ws =>
      ws.addQueryStringParameters("dataContractType" -> id)
        .get()
    }
  }

  def lookupDataContractProposal(id: String) = Action.async { request =>
    val encodedId = URLEncoder.encode(id, "UTF-8")
    val url = s"${chain_service_host}api/DataContract/$encodedId"
    enrichAndProxyRequest(request, url){_.get()}
  }

  def removeDataContractProposal(id: String) = Action.async { request =>
    val encodedId = URLEncoder.encode(id, "UTF-8")
    val url = s"${chain_service_host}api/DataContract/$encodedId"
    enrichAndProxyRequest(request, url){_.delete()}
  }

  def updateDataContractProposal(id: String) = Action(parse.json).async { request: Request[JsValue] =>
    val encodedId = URLEncoder.encode(id, "UTF-8")
    val url = s"${chain_service_host}api/DataContract/$encodedId"
    enrichAndProxyRequest(request, url){_.put(request.body)}
  }

  def lookupBusinessDataSetsByProvider(id: String) = Action.async { request =>
    val url = s"${chain_service_host}/api/queries/getDataContractsByProvider"
    enrichAndProxyRequest(request, url){ws =>
      ws.addQueryStringParameters("providerID" -> id)
        .get()
    }
  }

  def lookupBusinessDataSetsByConsumer(id: String) = Action.async { request  =>
    val url = s"${chain_service_host}/api/queries/getDataContractsByConsumer"
    enrichAndProxyRequest(request, url){ws =>
      ws.addQueryStringParameters("consumerID" -> id)
        .get()
    }
  }

}


