sap.ui.define([
	"com/inova8/lens/controller/BaseController"
], function(Controller) {
	"use strict";

	return Controller.extend("com.inova8.lens.controller.Home", {

			onInit: function () {

				this.getRouter().navTo("FileFormats", { "objectId":"FileFormat" ,"navigation": "", "?queryOptions":{}});
			}

	});

});