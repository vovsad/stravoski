var app = angular.module('StravoSki', ['ui.bootstrap']);



app.controller("TopCtrl", function($scope, $http, $modal, $log) {
	
	
	$scope.isLoadingActivities = true;
	$scope.isUpdatingSkiTracks = false;
	$scope.isLoadingFriends = true;
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
	
	
	

	$scope.activitiesCurrentPage = 0;
	$scope.activitiesPerPage = 10;
	$scope.loadActivities = function () {
		$http.get('/getactivities').
	    success(function(data, status, headers, config) {
	      $scope.activities = data;
	      $scope.pagedActivities = $scope.activities.slice(
	    		  $scope.activitiesCurrentPage*$scope.activitiesPerPage, 
	    		  $scope.activitiesCurrentPage*$scope.activitiesPerPage + $scope.activitiesPerPage);
	      $scope.isLoadingActivities = false;
	      $scope.showTopMenu = true;
	    }).
	    error(function(data, status, headers, config) {
	    	$scope.message = "Something goes wrong";
	    });
		
	};
	$scope.loadMoreActivities = function() {
		$scope.activitiesCurrentPage++;
	    var newActivities = $scope.activities.slice(
	    		$scope.activitiesCurrentPage*$scope.activitiesPerPage, 
	    		$scope.activitiesCurrentPage*$scope.activitiesPerPage + $scope.activitiesPerPage);
	    $scope.pagedActivities = $scope.pagedActivities.concat(newActivities);
	};
	
	
	$scope.nextPageDisabledClass = function(current, count, lendth) {
		console.log("current, count, lendth");
		console.log(current +","+ count +","+ lendth);
		return current === count - 1 || typeof lendth === "undefined"  || lendth === 0 ? "hidden" : "";
	};
	$scope.pageCount = function(length, perPage) {
		return Math.ceil(length/perPage);
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
	
	
	
	
	$scope.friendsCurrentPage = 0;
	$scope.friendsPerPage = 5;

	$scope.loadFriends = function () {
		$http.get('/getfriends').
	    success(function(data, status, headers, config) {
	      $scope.friends = data;
	      $scope.pagedFriends = $scope.friends.slice(
	    		  $scope.friendsCurrentPage*$scope.friendsPerPage, 
	    		  $scope.friendsCurrentPage*$scope.friendsPerPage + $scope.friendsPerPage);

	      
	      $scope.isLoadingFriends = false;
	    }).
	    error(function(data, status, headers, config) {
	    	$scope.message = "Something goes wrong";
	    });
	};
	$scope.loadMoreFriends = function() {
		$scope.friendsCurrentPage++;
	    var newFriends = $scope.friends.slice(
	    		$scope.friendsCurrentPage*$scope.friendsPerPage, 
	    		$scope.friendsCurrentPage*$scope.friendsPerPage + $scope.friendsPerPage);
	    $scope.pagedFriends = $scope.pagedFriends.concat(newFriends);
	};

	
	$scope.loadFriends();
	
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
	
	$scope.modalTemplete = 'modalContent.html';
	$scope.modalDialog = function(title, body, image) {
		var modalInstance = $modal.open({
		      animation: $scope.animationsEnabled,
		      templateUrl: $scope.modalTemplete,
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
		
		$scope.modalTemplete = 'modalContent.html';
		$scope.modalDialog(a.name, details, mapURL);
	  };
	  
	$scope.modalDialogFriendCompare = function (f){
		  $http.get('/getathletestat/' + f).
		    success(function(data, status, headers, config) {
		    	$scope.modalTemplete = 'modalDialogFriendCompareContent.html';
		    		      
		    	var comparedData = new Object();
		    	comparedData.friendsName = data.firstName;
		    	comparedData.yoursTotalDistance = $scope.statistics.totalDistance;
		    	comparedData.friendsTotalDistance = data.totalDistance;
		    	comparedData.yoursLongestDayActivityDistance = $scope.statistics.longestDayActivity.distance;
		    	comparedData.friendsLongestDayActivityDistance = data.longestDayActivity.distance;
		    	comparedData.yoursSkiedKmThisSeason = $scope.statistics.skiedKmThisSeason;
		    	comparedData.friendsSkiedKmThisSeason = data.skiedKmThisSeason;

		    	$scope.modalDialog('Compete with friends!', comparedData);
		    }).
		    error(function(data, status, headers, config) {
		    	$scope.message = "Something goes wrong";
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

