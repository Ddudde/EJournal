window.onload = function() {
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
	displayRequestDuration : true,
	operationsSorter : "method",
	validatorUrl : ""
  });
};
