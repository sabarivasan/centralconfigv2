package com.centralconfig.dw;

import com.centralconfig.persist.ConsulKVStore;
import com.centralconfig.persist.KVStore;
import com.centralconfig.resources.DocumentResource;
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

//      environment.jersey().register(new ConfigGenResource(config.getCentralConfigConfig()));
//      environment.jersey().register(new AuditTrailResource(config.getCentralConfigConfig()));
      environment.jersey().register(new DocumentResource(config, kvStore));
   }

   public static void main(String[] args) throws Exception {
       new CentralConfigService().run(args);
   }

}
