package cn.com.datasolution.dkservice.controller;

import cn.com.datasolution.config.properties.*;
import cn.com.datasolution.dkservice.pojo.JsonResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.RandomStringUtils;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LogLevel;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.web.bind.annotation.*;

import java.io.File;
@Slf4j
@RestController
@RequestMapping("kettle")
public class KettleController {
    @Autowired
    private KettleProperties kettleProperties;
    @Value("${kettle.trans_path}")
    String transPath;
    @Value("${kettle.pdi_path}")
    String pdiPath;

    public KettleController() {

    }

    @EventListener
    public void ApplicationReadyNotify(ApplicationReadyEvent event) {
          try {
            log.info("ApplicationReadyNotify...\n");
            System.setProperty( "pentaho.user.dir", pdiPath);
            System.setProperty( "DI_HOME", pdiPath);
            log.info("KettleEnvironment initing...\n");
            KettleEnvironment.init();
            log.info("KettleEnvironment init finished\n");
        } catch ( KettleException e ) {
            e.printStackTrace();
            return;
        }
    }

    @GetMapping("runtrans")
    public JsonResult runtrans() {
        Trans trans = runTransformationFromFileSystem(kettleProperties.getTrans_path() + "tran_calc_scores.ktr");
        return new JsonResult("0","success","");
    }

    @GetMapping("runtcmc")
    public JsonResult runtcmc() {
        log.info("runtcmc\n");
        Trans trans = runTransformationFromFileSystem(kettleProperties.getTrans_path() + "tran_cross_mysql_clickhouse.ktr");
        return new JsonResult("0","success","");
    }

    public Trans runTransformationFromFileSystem(String filename ) {

        try {
            System.out.println( "***************************************************************************************" );
            System.out.println( "Attempting to run transformation " + filename + " from file system" );
            System.out.println( "***************************************************************************************\n" );
            // Loading the transformation file from file system into the TransMeta object.
            // The TransMeta object is the programmatic representation of a transformation definition.
            TransMeta transMeta = new TransMeta( filename, (Repository) null );

            // The next section reports on the declared parameters and sets them to arbitrary values
            // for demonstration purposes
            System.out.println( "Attempting to read and set named parameters" );
            String[] declaredParameters = transMeta.listParameters();
            for ( int i = 0; i < declaredParameters.length; i++ ) {
                String parameterName = declaredParameters[i];

                // determine the parameter description and default values for display purposes
                String description = transMeta.getParameterDescription( parameterName );
                String defaultValue = transMeta.getParameterDefault( parameterName );
                // set the parameter value to an arbitrary string
                String parameterValue =  RandomStringUtils.randomAlphanumeric( 10 );

                String output = String.format( "Setting parameter %s to \"%s\" [description: \"%s\", default: \"%s\"]",
                        parameterName, parameterValue, description, defaultValue );
                System.out.println( output );

                // assign the value to the parameter on the transformation
                transMeta.setParameterValue( parameterName, parameterValue );
            }

            // Creating a transformation object which is the programmatic representation of a transformation
            // A transformation object can be executed, report success, etc.
            Trans transformation = new Trans( transMeta );

            // adjust the log level
            transformation.setLogLevel( LogLevel.DEBUG );

            System.out.println( "\nStarting transformation" );

            // starting the transformation, which will execute asynchronously
            transformation.execute( new String[0] );

            // waiting for the transformation to finish
            transformation.waitUntilFinished();

            // retrieve the result object, which captures the success of the transformation
            Result result = transformation.getResult();

            // report on the outcome of the transformation
            String outcome = String.format( "\nTrans %s executed %s", filename,
                    ( result.getNrErrors() == 0 ? "successfully" : "with " + result.getNrErrors() + " errors" ) );
            System.out.println( outcome );

            return transformation;
        } catch ( Exception e ) {

            // something went wrong, just log and return
            e.printStackTrace();
            return null;
        }
    }
}
