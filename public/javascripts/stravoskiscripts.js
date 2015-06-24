/**
 * 
 */

var app = angular.module('StravoSki', ['ui.bootstrap']);

app.controller("TopCtrl", function($scope, $http, $modal, $log) {

	$scope.loadAthleteStatistics = function () {
		  $http.get('/getathletestat').
		    success(function(data, status, headers, config) {
		      $scope.statistics = data;
		    }).
		    error(function(data, status, headers, config) {
		    	$scope.message = "Something goes wrong";
		    });
	};
	$scope.loadAthleteStatistics();

	$scope.loadActivities = function () {
	  $http.get('/getactivities').
	    success(function(data, status, headers, config) {
	      $scope.activities = data;
	    }).
	    error(function(data, status, headers, config) {
	    	$scope.message = "Something goes wrong";
	    });
	};
	$scope.loadActivities();




	  
	$scope.modalDialog = function(title, body, image) {
	
			var modalInstance = $modal.open({
			      animation: $scope.animationsEnabled,
			      templateUrl: 'modalContent.html',
		      controller: 'ModalInstanceCtrl',
		      resolve: {
		        messageBody: function () {
		        	return body;
		        },
		        messageTitle: function () {
		        	return title;
		        },
		        messageImage: function () {
		        	return image;
		        }
		      }
		    });
	
		    modalInstance.result.then(function (selectedItem) {
		      $scope.selected = selectedItem;
		    }, function () {
		      $log.info('Modal dismissed at: ' + new Date());
		    });
	};
		
	$scope.modalDialogActivityDetails = function (a){
		var mapURL = 'http://maps.googleapis.com/maps/api/staticmap?sensor=false&size=150x150&path=weight:3|color:red|enc:';
		mapURL += a.map.summary_polyline;
		var details = 'Skiied ' + Math.round(a.distance/1000) + ' km ';
		if(a.location_city != null){
			details += 'at ' + a.location_city;
		}else if(a.location_state != null){
			details += 'at ' + a.location_state;
		}
		details += ' for ' + Math.floor(a.moving_time/3600) 
		  				+ ':' + (Math.floor(a.moving_time/60) - Math.floor(a.moving_time/3600)*60) + ' moving time.';
		  
		$scope.modalDialog(a.name, details, mapURL);
	  };
	
	$scope.doSyncWithStrava = function () {
      	$http.get('/syncwithstrava').
	    success(function(data, status, headers, config) {
	    	$scope.modalDialog('Notification', 
	    			'Your Activities are just synced and cached');
	    	$scope.loadActivities();
	    }).
	    error(function(data, status, headers, config) {
	    	$scope.modalDialog('Error', 
			'Something goes wrong');
	    });
	};

});

app.controller('ModalInstanceCtrl', function ($scope, $modalInstance, messageBody, messageTitle, messageImage) {
	$scope.messageBody = messageBody;
	$scope.messageTitle = messageTitle;
	$scope.messageImage = messageImage;
	  $scope.ok = function () {
	    $modalInstance.close('ok');
	  };
});

