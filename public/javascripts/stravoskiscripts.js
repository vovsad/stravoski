var app = angular.module('StravoSki', ['ui.bootstrap']);



app.controller("TopCtrl", function($scope, $http, $modal, $log) {
	
	
	$scope.isLoadingActivities = true;
	$scope.isUpdatingSkiTracks = false;
	$scope.showTopMenu = false;
	
	$scope.isDataSynced = function () {
		$http.get('/isdatasynced').
	    success(function(data, status, headers, config) {
	    	if(data.isDataSynced){
	    		$scope.loadActivities();
		        $scope.loadAthleteStatistics();
	    	}else{
	    		$scope.doDataSync();
	    	}
	    }).
	    error(function(data, status, headers, config) {
	    	$scope.message = "Something goes wrong";
	    });
		
	};
	
	$scope.doDataSync = function () {
		$http.get('/dosync').
	    success(function(data, status, headers, config) {
	    	$scope.loadActivities();
	        $scope.loadAthleteStatistics();
	        $scope.updateToSki();
	    }).
	    error(function(data, status, headers, config) {
	    	$scope.message = "Something goes wrong";
	    });
		
	};
	
	$scope.isDataSynced();
	

	$scope.currentPage = 0;
	$scope.activitiesPerPage = 10;

	$scope.loadActivities = function () {
		$http.get('/getactivities').
	    success(function(data, status, headers, config) {
	      $scope.activities = data;
	      $scope.total = data.length;
	      $scope.pagedActivities = $scope.activities.slice(
	    		  $scope.currentPage*$scope.activitiesPerPage, 
	    		  $scope.currentPage*$scope.activitiesPerPage + $scope.activitiesPerPage);
	      $scope.isLoadingActivities = false;
	      $scope.showTopMenu = true;
	    }).
	    error(function(data, status, headers, config) {
	    	$scope.message = "Something goes wrong";
	    });
		
	};
	
	$scope.updateToSki = function () {
		$scope.isUpdatingSkiTracks = true;
		$http.get('/updatetoski').
	    success(function(data, status, headers, config) {
	      if(data.isAnythingUpdated) $scope.loadActivities();
	      $scope.isUpdatingSkiTracks = false;
	    }).
	    error(function(data, status, headers, config) {
	    	$scope.message = "Something goes wrong";
	    });
		
	};
	
	
	$scope.loadAthleteStatistics = function () {
		  $http.get('/getathletestat').
		    success(function(data, status, headers, config) {
		      $scope.statistics = data;
		    }).
		    error(function(data, status, headers, config) {
		    	$scope.message = "Something goes wrong";
		    });
	};
	
	$scope.loadFriends = function () {
		$http.get('/getfriends').
	    success(function(data, status, headers, config) {
	      $scope.friends = data;
	    }).
	    error(function(data, status, headers, config) {
	    	$scope.message = "Something goes wrong";
	    });
		
	};	
	$scope.loadFriends();
	
	$scope.loadMoreActivities = function() {
		$scope.currentPage++;
	    var newActivities = $scope.activities.slice(
	    		$scope.currentPage*$scope.activitiesPerPage, 
	    		$scope.currentPage*$scope.activitiesPerPage + $scope.activitiesPerPage);
	    $scope.pagedActivities = $scope.pagedActivities.concat(newActivities);
	};

	$scope.nextPageDisabledClass = function() {
		return $scope.currentPage === $scope.pageCount()-1 || typeof $scope.total === "undefined"  || $scope.total === 0 ? "hidden" : "";
	};

	$scope.pageCount = function() {
		return Math.ceil($scope.total/$scope.activitiesPerPage);
	};		

	$scope.doSyncWithStrava = function () {
      	$http.get('/syncwithstrava').
	    success(function(data, status, headers, config) {
	    	$scope.modalDialog('Notification', 
	    			'Your Activities are just synced and cached');

	    	$scope.loadActivities();
	    	$scope.loadAthleteStatistics();

	    }).
	    error(function(data, status, headers, config) {
	    	$scope.modalDialog('Error', 
			'Something goes wrong');
	    });
	};

	$scope.doLogout = function () {
		$location.path('/logout');
	   	$scope.isLoaded = false;
	};
	
	
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
	        	return typeof image === 'undefined'? '':image;
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
		
		var minutes = function(seconds){
			var result = (Math.floor(seconds/60) - Math.floor(seconds/3600)*60);
			return (result > 9)? result: '0' + result;
		}
		
		var hours = function(seconds){
			return Math.floor(seconds/3600);
		}
		var details = 'Skiied ' + Math.round(a.distance/1000) + ' km ';
		if(a.location_city != null){
			details += 'at ' + a.location_city;
		}else if(a.location_state != null){
			details += 'at ' + a.location_state;
		}
		details += ' for ' + hours(a.moving_time) 
		  				+ ':' + minutes(a.moving_time) + ' moving time';
		details += ' and ' + hours(a.elapsed_time) 
				+ ':' + minutes(a.elapsed_time) + ' elapsed time. ';
		
		details += 'Downhill distance without ski lifts is ' + Math.round(a.downhill_distance/1000) + 'km .';
		  
		$scope.modalDialog(a.name, details, mapURL);
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

