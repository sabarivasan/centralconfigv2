package com.centralconfig.dw;

import com.centralconfig.persist.ConsulKVStore;
import com.centralconfig.persist.KVStore;
import com.centralconfig.persist.TimedConsulKVStore;
import com.centralconfig.persist.TimedDocumentStore;
import com.centralconfig.persist.TimedKVStore;
import com.centralconfig.publish.ConfigChangePublisher;
import com.centralconfig.publish.DocumentDependencyManager;
import com.centralconfig.resources.DocumentResource;
import com.centralconfig.resources.TimedDocumentResource;
import com.cvent.CventApplication;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

/**
 * Created by sviswanathan on 2/15/16.
 */
public class CentralConfigService extends CventApplication<CentralConfigConfiguration> {

   @Override
   public String getName() {
      return "config-service";
   }

   @Override
   public void initialize(Bootstrap<CentralConfigConfiguration> bootstrap) {
      super.initialize(bootstrap);
   }

   @Override
   public void run(CentralConfigConfiguration config, Environment environment) throws Exception {
      super.run(config, environment);

      KVStore kvStore = new ConsulKVStore(config.getConsulEndpoint());
      TimedKVStore timedKVStore = new TimedConsulKVStore(config.getConsulEndpoint());
      TimedDocumentStore timedDocStore = new TimedDocumentStore(timedKVStore);
      ConfigChangePublisher configChangePublisher = new ConfigChangePublisher(config);
      DocumentDependencyManager docDependencyManager = new DocumentDependencyManager(config, kvStore,
                                                                                     configChangePublisher);

//      environment.jersey().register(new ConfigGenResource(config.getCentralConfigConfig()));
//      environment.jersey().register(new AuditTrailResource(config.getCentralConfigConfig()));
      environment.jersey().register(new DocumentResource(config, kvStore, configChangePublisher,
                                                         docDependencyManager));
      environment.jersey().register(new TimedDocumentResource(config, kvStore, timedDocStore, configChangePublisher,
                                                              docDependencyManager));
   }

   public static void main(String[] args) throws Exception {
       new CentralConfigService().run(args);
   }

}
