var app = angular.module('StravoSki', ['ui.bootstrap', 'ngMap']);

app.controller("TopCtrl", function($scope, $http, $modal, $log) {
	$scope.isLoadingActivities = true;
	$scope.isUpdatingSkiTracks = false;
	$scope.isLoadingFriends = true;
	$scope.showTopMenu = false;
	
	$scope.isDataSynced = function () {
		$http.get('/isdatasynced', getAuthCookies()).
	    success(function(data, status, headers, config) {
	    	if(data.isDataSynced){
	    		$scope.loadActivities();
		        $scope.loadAthleteStatistics();
		        $scope.loadFriends();
	    	}else{
	    		$scope.doDataSync();
	    	}
	    }).
	    error(function(data, status, headers, config) {
	    	$scope.message = "Something goes wrong";
	    });
		
	};
	
	$scope.doDataSync = function () {
		$http.get('/dosync', getAuthCookies()).
	    success(function(data, status, headers, config) {
	    	$scope.loadActivities();
	        $scope.loadAthleteStatistics();
	        $scope.loadFriends();
	        $scope.updateToSki();
	    }).
	    error(function(data, status, headers, config) {
	    	$scope.message = "Something goes wrong";
	    });
		
	};
	
	if (getCookie("AUTH_TOKEN") != null)
		$scope.isDataSynced();
	
	
	

	$scope.activitiesCurrentPage = 0;
	$scope.activitiesPerPage = 10;
	$scope.loadActivities = function () {
		$http.get('/getactivities', getAuthCookies()).
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
		return current === count - 1 || typeof lendth === "undefined"  || lendth === 0 ? "hidden" : "";
	};
	$scope.pageCount = function(length, perPage) {
		return Math.ceil(length/perPage);
	};		
	
	

	
	$scope.updateToSki = function () {
		$scope.isUpdatingSkiTracks = true;
		$http.get('/updatetoski', getAuthCookies()).
	    success(function(data, status, headers, config) {
	      if(data.isAnythingUpdated) $scope.loadActivities();
	      $scope.isUpdatingSkiTracks = false;
	    }).
	    error(function(data, status, headers, config) {
	    	$scope.message = "Something goes wrong";
	    });
		
	};
	
	
	
	
	$scope.loadAthleteStatistics = function () {
		  $http.get('/getathletestat', getAuthCookies()).
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
		$http.get('/getfriends', getAuthCookies()).
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

	
	$scope.doSyncWithStrava = function () {
      	$http.get('/syncwithstrava', getAuthCookies()).
	    success(function(data, status, headers, config) {
	    	$scope.modalTemplete = 'modalContent.html';
	    	$scope.modalDialog('Notification', 
	    			'Your Activities are just synced and cached');

	    	$scope.loadActivities();
	    	$scope.loadAthleteStatistics();

	    }).
	    error(function(data, status, headers, config) {
	    	$scope.modalTemplete = 'modalContent.html';
	    	$scope.modalDialog('Error', 
			'Something goes wrong');
	    });
	};

	$scope.doLogout = function () {
		deleteCookie("AUTH_TOKEN");deleteCookie("ATHLETE_ID");
		$location.path('/logout');
	   	$scope.isLoaded = false;
	};
	
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
	
	
	$scope.modalDialogActivityDetails = function (id){
		var a = $scope.activities.filter(function(item) {
			  if(item.id === id) {
				    return item;
				  }
				})[0];
		console.log(a);
		
		var mapURL = 'http://maps.googleapis.com/maps/api/staticmap?sensor=false&size=150x150&path=weight:3|color:red|enc:';
		
		var activityModalData = new Object();
		activityModalData.moving_time = timeFromSeconds(a.moving_time);
		activityModalData.elapsed_time = timeFromSeconds(a.elapsed_time);
		activityModalData.downhill_distance = Math.round(a.downhill_distance/1000);
		activityModalData.distance = Math.round(a.distance/1000);
		activityModalData.max_speed = Math.round(a.max_speed*3600/1000);
		activityModalData.average_speed = Math.round(a.average_speed*3600/1000);
		activityModalData.URL = mapURL += a.map.summary_polyline; 
		activityModalData.polyline = a.map.summary_polyline;
		activityModalData.lat = a.start_lat;
		activityModalData.lng = a.start_lng;
		activityModalData.average_downhill_grade = a.average_downhill_grade;
	
		if(a.location_city != null){
			activityModalData.location = a.location_city;
		}else if(a.location_state != null){
			activityModalData.location = a.location_state;
		}else{
			activityModalData.location = 'is unknown';
		}

		$scope.modalTemplete = 'modalDialogActivityDetails.html';
		$scope.modalDialog(a.name, activityModalData);
	  };
	  
	$scope.modalDialogFriendCompare = function (f){
		  $http.get('/getathletestat/' + f, getAuthCookies()).
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
	  
app.controller('ModalInstanceCtrl', function ($scope, $modalInstance, $timeout, messageBody, messageTitle) {
	$scope.messageBody = messageBody;
	$scope.messageTitle = messageTitle;
	$scope.decodedPath = google.maps.geometry.encoding.decodePath(messageBody.polyline);
    $scope.lat = messageBody.lat;
	$scope.lng = messageBody.lng;
	
	$scope.path = [];
	for (i =0; i < $scope.decodedPath.length; i++){
		$scope.path[i] = [$scope.decodedPath[i].H, $scope.decodedPath[i].L];
	}

	$scope.render = true;
	  $scope.ok = function () {
	    $modalInstance.close('ok');
	  };

	  var marker, map; 
	  $scope.$on('mapInitialized', function(evt, evtMap){ 
		  map = evtMap;
		  map.panTo(new google.maps.LatLng($scope.lat, $scope.lng));
		  }); 
 
});


function getCookie(name) {
	  var value = "; " + document.cookie;
	  var parts = value.split("; " + name + "=");
	  if (parts.length == 2) return parts.pop().split(";").shift();
	}

function getAuthCookies(){
	return {headers: {
		'AUTH_TOKEN': getCookie("AUTH_TOKEN"),
		'ATHLETE_ID': getCookie("ATHLETE_ID")
	}
	};
}

function deleteCookie(name) {
    document.cookie = encodeURIComponent(name) + "=deleted; expires=" + new Date(0).toUTCString();
}

function timeFromSeconds(time){
	var minutes = function(seconds){
		var result = (Math.floor(seconds/60) - Math.floor(seconds/3600)*60);
		return (result > 9)? result: '0' + result;
	}
	
	var hours = function(seconds){
		return Math.floor(seconds/3600);
	}
	
	return hours(time) + ':' + minutes(time);
}