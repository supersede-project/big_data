/**
 * Created by snadal on 17/05/16.
 */

var config = {};

config.PORT = 3000;
config.METADATA_FRONTEND_URL = "http://localhost:"+config.PORT+"/";
config.METADATA_DATA_LAYER_URL = "http://localhost:8082/metadataStorage/";
config.ONTO_MATCH_MERGE_URL = "http://localhost:8082/ontoMatchMerge/";
config.DEFAULT_NAMESPACE = "http://www.essi.upc.edu/~snadal/";
config.FILES_PATH = "/Users/snadal/UPC/Sergi/SUPERSEDE/Development/big_data/data_management/MetadataManagementSystem/MetadataFrontend/MDMfiles/";

module.exports = config;