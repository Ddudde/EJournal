window.onload = function() {
  //<editor-fold desc="Changeable Configuration Block">

  // the following lines will be replaced by docker/configurator, when it runs in a docker-container
  window.ui = SwaggerUIBundle({
    url: "",
    dom_id: '#swagger-ui',
    deepLinking: true,
    presets: [
      SwaggerUIBundle.presets.apis,
      SwaggerUIStandalonePreset
    ],
    plugins: [
      SwaggerUIBundle.plugins.DownloadUrl
    ],
    layout: "StandaloneLayout",
	urls: [
		{
			url: "swagger.json",
			name: "default"
		}
	],
	//configUrl : "/demo-spring-boot-3-webmvc/v3/api-docs/swagger-config",
	displayRequestDuration : true,
	operationsSorter : "method",
	validatorUrl : ""
  });

  //</editor-fold>
};