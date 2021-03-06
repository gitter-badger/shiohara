shioharaApp.controller('ShObjectChildrenCtrl', [
						"$scope"
						, "$state"
						, "$stateParams"
						, "$rootScope"
						, "$translate"
						, "$http"
						, "$window"
						, "shAPIServerService"
						, 'vigLocale'
						, "shFolderFactory"
						, "shPostFactory"
						, "ShDialogSelectObject"
						, "ShDialogDeleteFactory"
						, "shPostResource"
						, "shFolderResource"
						, "$filter"
						, "Notification"
						, "moment"
						, "shUserResource"
						, "shPostTypeResource"
    , function ($scope, $state, $stateParams, $rootScope, $translate, $http, $window, shAPIServerService, vigLocale, shFolderFactory, shPostFactory, ShDialogSelectObject, ShDialogDeleteFactory, shPostResource, shFolderResource, $filter, Notification, moment, shUserResource, shPostTypeResource) {
        
		$scope.objectId = $stateParams.objectId;
        $scope.vigLanguage = vigLocale.getLocale().substring(0, 2);
        $translate.use($scope.vigLanguage);
        $scope.shCurrentFolder = null;
        $scope.shSite = null;
        $scope.shFolders = null;
        $scope.shPosts = null;
        $scope.breadcrumb = null;
        $rootScope.$state = $state;
        $scope.shStateObjects = [];
        $scope.shObjects = [];
        $scope.actions = [];
        $scope.shUser = null;
        $scope.shLastPostType = null;
        
    	$scope.shUser = shUserResource.get({
			id : 1,
			access_token : $scope.accessToken
		}, function() {
			$scope.shLastPostType = shPostTypeResource.get({
				id : $scope.shUser.lastPostType
			});
			
		});
    	 $scope.$evalAsync($http.get(shAPIServerService.get().concat("/v2/history/object/" + $scope.objectId ))
    			 .then(function (response) {
             $scope.commits = response.data.length;
             console.log ($scope.commits);
         }));
    	 
        $scope.$evalAsync($http.get(shAPIServerService.get().concat("/v2/object/" + $scope.objectId + "/list")).then(function (response) {
            $scope.processResponse(response);
        }));
        $scope.processResponse = function (response) {
            $scope.shFolders = response.data.shFolders;
            $scope.shPosts = response.data.shPosts;
            $scope.breadcrumb = response.data.breadcrumb;         
            $scope.shCurrentFolder = null;
            if ($scope.breadcrumb != null) {	
            	$scope.shCurrentFolder = $scope.breadcrumb.slice(-1).pop();
            }
            $scope.shSite = response.data.shSite;         
            angular.forEach($scope.shFolders, function (shFolder, key) {
                $scope.shStateObjects[shFolder.shGlobalId.id] = false;
                $scope.shObjects[shFolder.shGlobalId.id] = shFolder;
                $scope.actions[shFolder.shGlobalId.id] = false;
            });
            angular.forEach($scope.shPosts, function (shPost, key) {
                $scope.shStateObjects[shPost.shGlobalId.id] = false;
                $scope.shObjects[shPost.shGlobalId.id] = shPost;
                $scope.actions[shPost.shGlobalId.id] = false;
            });
        }
        
        $scope.selectContents = function () {	
       	 for (var stateKey in $scope.shStateObjects) {
       		 if ($scope.shObjects[stateKey].shGlobalId.type === "POST") {
       			 $scope.shStateObjects[stateKey] = true;
       		 }
       		 else {
       			$scope.shStateObjects[stateKey] = false;
       		 }
       	 }
        }
        
        $scope.selectFolders = function () {	
        	 for (var stateKey in $scope.shStateObjects) {
        		 if ($scope.shObjects[stateKey].shGlobalId.type === "FOLDER") {
        			 $scope.shStateObjects[stateKey] = true;
        		 }
        		 else {
        			 $scope.shStateObjects[stateKey] = false;
        		 }
        	 }
        }
        
        $scope.selectEverything = function () {	
          	 for (var stateKey in $scope.shStateObjects) {          		
          			 $scope.shStateObjects[stateKey] = true;
          	 }
        }
        
        $scope.selectNothing = function () {	
         	 for (var stateKey in $scope.shStateObjects) {          		
         			 $scope.shStateObjects[stateKey] = false;
         	 }
        }
        
        $scope.selectInverted = function () {	
        	 for (var stateKey in $scope.shStateObjects) {  
        		 if ($scope.shStateObjects[stateKey]) {
        			 $scope.shStateObjects[stateKey] = false;
        		 } else {
        			 $scope.shStateObjects[stateKey] = true;
        		 }
        		 
        	 }
       }

        $scope.updateAction = function (shGlobalId, value) {
            $scope.actions[shGlobalId.id] = value;
        }
        $scope.isRecent = function (date) {
        	var momentDate = moment(date);
        	var now = new moment();
        	var duration = moment.duration(momentDate.diff(now))        
        	if (duration.as('minutes') >= -5) {        		
        		return true;
        	}
        	else {
        		return false;
        	}
        	
            return false;
        }
        $scope.objectsCopy = function () {
            var objectGlobalIds = [];
            for (var stateKey in $scope.shStateObjects) {
                if ($scope.shStateObjects[stateKey] === true) {
                    var objectGlobalId = "" + stateKey;
                    objectGlobalIds.push(objectGlobalId);
                }
            }
            $scope.objectsCopyDialog(objectGlobalIds);
        }
        
        $scope.objectCopy = function (shObject) {
            var objectGlobalIds = [];
            objectGlobalIds.push(shObject.shGlobalId.id);
            $scope.objectsCopyDialog(objectGlobalIds);
        }
        
        $scope.objectsCopyDialog = function (objectGlobalIds) {
            var modalInstance = ShDialogSelectObject.dialog($scope.objectId, "shFolder");
            modalInstance.result.then(function (shObjectSelected) {
                var parameter = JSON.stringify(objectGlobalIds);
                $http.put(shAPIServerService.get().concat("/v2/object/copyto/" + shObjectSelected.shGlobalId.id), parameter).then(function (response) {
                    var shObjects = response.data;
                    for (i = 0; i < shObjects.length; i++) {
                    	shObject = shObjects[i];
                    	var copiedMessage = null;
                        if (shObject.shGlobalId.type == "POST") {
                        	copiedMessage = 'The ' + shObject.title + ' Post was copied.';
                        }
                        else if (shObject.shGlobalId.type == "FOLDER") {
                        	copiedMessage = 'The ' + shObject.name + ' Folder was copied.';
                        }
                        Notification.warning(copiedMessage);
                    }
                });
            }, function () {
                // Selected CANCEL
            });
        }
        $scope.objectsMove = function () {
            var objectGlobalIds = [];
            for (var stateKey in $scope.shStateObjects) {
                if ($scope.shStateObjects[stateKey] === true) {
                    var objectGlobalId = "" + stateKey;
                    objectGlobalIds.push(objectGlobalId);
                }
            }
            $scope.objectsMoveDialog(objectGlobalIds);
        }
        
        $scope.objectMove = function (shObject) {
            var objectGlobalIds = [];
            objectGlobalIds.push(shObject.shGlobalId.id);
            $scope.objectsMoveDialog(objectGlobalIds);
        }
        
        $scope.objectsMoveDialog = function (objectGlobalIds) {
            var modalInstance = ShDialogSelectObject.dialog($scope.objectId, "shFolder");
            modalInstance.result.then(function (shObjectSelected) {
                var parameter = JSON.stringify(objectGlobalIds);
                $http.put(shAPIServerService.get().concat("/v2/object/moveto/" + shObjectSelected.shGlobalId.id), parameter).then(function (response) {
                    var shObjects = response.data;
                    for (i = 0; i < shObjects.length; i++) {
                    	shObject = shObjects[i];
                        var movedMessage = null;
                        if (shObject.shGlobalId.type == "POST") {
                        	movedMessage = 'The ' + shObject.title + ' Post was moved.';
	                        var foundItem = $filter('filter')
	                            ($scope.shPosts, {
	                                id: shObject.id
	                            }, true)[0];
	                        var index = $scope.shPosts.indexOf(foundItem);
	                        $scope.shPosts.splice(index, 1);
                        } else if (shObject.shGlobalId.type == "FOLDER") {
                        	movedMessage = 'The ' + shObject.name + ' Folder was moved.';
                        	  var foundItem = $filter('filter')
	                            ($scope.shFolders, {
	                                id: shObject.id
	                            }, true)[0];
	                        var index = $scope.shFolders.indexOf(foundItem);
	                        $scope.shFolders.splice(index, 1);
                        }
                        Notification.warning(movedMessage);
                    }
                });
            }, function () {
                // Selected CANCEL
            });
        }
        
        $scope.objectClone = function (shObject) {
            var objectGlobalIds = [];
            objectGlobalIds.push(shObject.shGlobalId.id);
            var parameter = JSON.stringify(objectGlobalIds);
            var parentObjectId = null;
            if ($scope.shCurrentFolder == null) {
            	parentObjectId = $scope.shSite.shGlobalId.id;
            }
            else {
            	parentObjectId = $scope.shCurrentFolder.shGlobalId.id;
            }
            $http.put(shAPIServerService.get().concat("/v2/object/copyto/" + parentObjectId), parameter).then(function (response) {
                var shObjects = response.data;
                for (i = 0; i < shObjects.length; i++) {
                	shObject = shObjects[i];
                	var clonedMessage = null;
                	if (shObject.shGlobalId.type == "POST") {
                		clonedMessage = 'The ' + shObject.title + ' Post was cloned.';                	
                	} else if (shObject.shGlobalId.type == "FOLDER") {
                		clonedMessage = 'The ' + shObject.name + ' Folder was cloned.';                		
                	}
                	Notification.warning(clonedMessage);
                }
            });
        }       
        $scope.objectsRename = function () {
            for (var stateKey in $scope.shStateObjects) {
                if ($scope.shStateObjects[stateKey] === true) {
                    console.log("Rename " + stateKey);
                }
            }
        }
        $scope.objectsDelete = function () {
        	 var shSelectedObjects = [];
             for (var stateKey in $scope.shStateObjects) {
                 if ($scope.shStateObjects[stateKey] === true) {                     
                	 shSelectedObjects.push($scope.shObjects[stateKey]);
                 }
             }
             $scope.objectsDeleteDialog(shSelectedObjects);
        }
        $scope.folderDelete = function (shFolder) {
            shFolderFactory.deleteFromList(shFolder, $scope.shFolders);
        }
        $scope.postDelete = function (shPost) {         
            shPostFactory.deleteFromList(shPost, $scope.shPosts);
        }
        
        
        $scope.objectsDeleteDialog = function (shSelectedObjects) {
            var modalInstance = ShDialogDeleteFactory.dialog(shSelectedObjects);
            modalInstance.result.then(function () {
            	angular.forEach(shSelectedObjects, function(value, key) {  
            		if (value.shGlobalId.type === "POST") {
            		shPost = value;
            		var deletedMessage = 'The ' + shPost.title + ' Post was deleted.';
                    shPostResource.delete({
                        id: shPost.id
                    }, function () {
                        // filter the array
                        var foundItem = $filter('filter')($scope.shPosts, {
                            id: shPost.id
                        }, true)[0];
                        // get the index
                        var index = $scope.shPosts.indexOf(foundItem);
                        // remove the item from array
                        $scope.shPosts.splice(index, 1);
                        Notification.error(deletedMessage);
                    });
            	} else if (value.shGlobalId.type === "FOLDER") {
            		shFolder = value;
            		var deletedMessage = 'The ' + shFolder.name + ' Folder was deleted.';
                    shFolderResource.delete({
                        id: shFolder.id
                    }, function () {
                        // filter the array
                        var foundItem = $filter('filter')($scope.shFolders, {
                            id: shFolder.id
                        }, true)[0];
                        // get the index
                        var index = $scope.shFolders.indexOf(foundItem);
                        // remove the item from array
                        $scope.shFolders.splice(index, 1);
                        Notification.error(deletedMessage);
                    });
            	}
            	});
            }, function () {
                // Selected CANCEL
            });
        }
        
        $scope.objectPreview = function (shObject) {
            var link = shAPIServerService.get().concat("/v2/object/" + shObject.shGlobalId.id + "/preview");
            $window.open(link);
        }
						}]);