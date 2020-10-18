object Server extends App with CorsSupport {
  implicit val system = ActorSystem("sangria-server")
  implicit val materializer = ActorMaterializer()

  import system.dispatcher

  def executeGraphQL(query: Document, operationName: Option[String], variables: Json, tracing: Boolean) = 
    complete(
      Executor.execute(
        SchemaDefinition.StarWaesSchema,
        query,
        new CharacterRepo,
        variables = if (variables.isNull) Json.obj() else variables,
        operationName = operationName,
        middleware = if (tracing) SlowLog.apolloTracing :: Nil else Nil,
        deferredResolver = DeferredResolver.fetchers(SchemaDefinition.Characters)
      )
      .map(OK -> _)
      .recover {
        case error: QueryAnalysisError => BadRequest -> error.resolveError
        case error: ErrorWithResolver => InternalServerError -> error.resolveError
      }
    )

  def formatError(error: Throwable): Json = error match {
    case e =>
      throw e
  }

  def formatError(message: String): Json = Json.obj("errors" -> Json.arr(
    Json.obj("message" -> Json.fromString(message))
  ))

  val route: Route = 
    path("graphql") {
      get {
        explicitlyAccepts(`text/html`) {
          getFromResource("assets/grapiql.html")
        }
      } ~
        parameters('query, 'operationName.?, 'variables.?) { (query, operationName, variables) => 
          QueryParser.parse(query) match {
            case Success(ast) =>
              variables.map(parse) match {
                case Some(Left(error)) => complete(BadRequest, formatError(error))
                case Some(Right(json)) => executeGraphQL(ast, operationName, json)
                case None => executeGraphQL(ast, operationName, Json.obj())
              }
            case Failure(error) => complete(BadRequest, formatError(error))
          }
        }
    } ~
      post {
        parameters() {}
      }
    } ~
    (get & pathEndOrSingleSlash) {
      redirect("/graphql", PermanentRedirect)
    }

  Http().bindAndHandle(corsHandler(route), "0.0.0.0", sys.props.get("http.port").fold(8080)(_.toInt))
}

