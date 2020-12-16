sap.ui.define([
	"com/inova8/lens/controller/BaseController",
		'sap/ui/model/json/JSONModel'
], function(Controller, JSONModel) {
	"use strict";

	return Controller.extend("com.inova8.lens.controller.Home", {

			onInit: function () {
				var oViewModel = new JSONModel({
					ColumnChartData: [{v: 80,l:"a"}, {v: 150,l:"b"}, {v: 400,l:"c"}, {v: 200,l:"d"}, {v: 201,l:"e"}, {v: 202,l:"f"}, {v: 203,l:"g"}],
					//ColumnChartData2: [{v: 40}, {v: 320}, {v: 270}, {v: 140}, {v: 60}],
					ComparisonChartData: [{v: 120}, {v: -67}, {v: 250}, {v: -80}],
				//	ComparisonChartData2: [{v: -70}, {v: 170}, {v: -30}, {v: 60}, {v: 120}],
					PieChartData: [{v: 83}]//,
				//	PieChartData2: [{v: 57}]
				});
				this.setModel(oViewModel, "view");

			}

		/**
		 * Similar to onAfterRendering, but this hook is invoked before the controller's View is re-rendered
		 * (NOT before the first rendering! onInit() is used for that one!).
		 * @memberOf com.inova8.lens.view.Home
		 */
		//	onBeforeRendering: function() {
		//
		//	},

		/**
		 * Called when the View has been rendered (so its HTML is part of the document). Post-rendering manipulations of the HTML could be done here.
		 * This hook is the same one that SAPUI5 controls get after being rendered.
		 * @memberOf com.inova8.lens.view.Home
		 */
		//	onAfterRendering: function() {
		//
		//	},

		/**
		 * Called when the Controller is destroyed. Use this one to free resources and finalize activities.
		 * @memberOf com.inova8.lens.view.Home
		 */
		//	onExit: function() {
		//
		//	}

	});

});