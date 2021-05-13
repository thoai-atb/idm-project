import java.util.HashMap;

import weka.clusterers.DBSCAN;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.EuclideanDistance;

public class DBScanClustering {
	private Instances instances;
	private int numberOfCluster;
	protected DBSCAN dbscan = new DBSCAN();
	
	public DBScanClustering(Instances instances) {
		this.instances = instances;
	}
	
	public void processing() throws Exception {
		this.dbscan.setEpsilon(0.8);
		this.dbscan.setMinPoints(5);
		this.dbscan.setDistanceFunction(new EuclideanDistance());
		this.dbscan.buildClusterer(this.instances);
		this.numberOfCluster = dbscan.numberOfClusters();
	}
	
	public int getNumberOfCluster() throws Exception {
		return this.numberOfCluster;
	}
	
	public int getCluster(Instance instance) throws Exception {
		return this.dbscan.clusterInstance(instance);
	}
	
	public HashMap<Integer, Instances> getClusteredInstances() throws Exception {
		HashMap<Integer, Instances> clusteredInstances = new HashMap<Integer, Instances>();
		for (int i = -1; i < this.numberOfCluster; i++) {
			clusteredInstances.put(new Integer(i), new Instances(this.instances, 0, 0));
		}
		for (int i = 0; i < this.instances.numInstances(); i++) {
			Instance instance = this.instances.get(i);
			int cluster;
			try {
				cluster = dbscan.clusterInstance(instance);
			} catch(Exception e) {// If the instance is considered as a noise, dbscan will throw an exception
				cluster = -1;
			}
			Instances oldInstances = clusteredInstances.get(cluster);
			oldInstances.add(instance);
			clusteredInstances.replace(cluster, oldInstances);
		}
		return clusteredInstances;
	}
	
}
