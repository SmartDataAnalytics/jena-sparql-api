package org.aksw.jena_sparql_api.batch.backend.sparql;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Root;

import org.aksw.jena_sparql_api.geo.vocab.BATCH;
import org.aksw.jena_sparql_api.mapper.MappedConcept;
import org.aksw.jena_sparql_api.shape.ResourceShape;
import org.aksw.jena_sparql_api.shape.ResourceShapeBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jena.graph.Graph;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.DCTerms;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameter.ParameterType;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.repository.dao.AbstractJdbcBatchMetadataDao;
import org.springframework.batch.core.repository.dao.JdbcJobExecutionDao;
import org.springframework.batch.core.repository.dao.JobExecutionDao;
import org.springframework.batch.core.repository.dao.NoSuchObjectException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.incrementer.DataFieldMaxValueIncrementer;
import org.springframework.util.Assert;

public class JobExecutionDaoSparql
	extends AbstractJdbcBatchMetadataDao
	implements JobExecutionDao, InitializingBean
{

    private static final Log logger = LogFactory.getLog(JdbcJobExecutionDao.class);

    protected EntityManager em;

    
    public JobExecutionDaoSparql(EntityManager em) {
    	this.em = em;
    }

    // TODO Essentially these are all the properties we have to deal with
    public static List<Property> properties = Arrays.asList(BATCH.jobExecutionId, BATCH.startTime, BATCH.endTime, BATCH.status, BATCH.exitCode, BATCH.exitMessage, DCTerms.created, DCTerms.modified, BATCH.version, BATCH.jobConfigurationLocation);

    
    public void createQuerySimpleInsert(List<Property> properties) {
        
    }
    
    //"INSERT INTO <%GRAPH%> { ? batch:jobExecutionId ? ; batch:jobInstanceId ? ; batch:startTime ? ; batch:endTime ? ; batch:status ? ; batch:exitCode ? ; batch:exitMessage ? ; batch:version ? ; dcterm:created ?   "
//    private static final String SAVE_JOB_EXECUTION = createQuerySimpleInsert()
//            + ""
//            + "%JOB_EXECUTION% (JOB_EXECUTION_ID, JOB_INSTANCE_ID, START_TIME, "
//            + "END_TIME, STATUS, EXIT_CODE, EXIT_MESSAGE, VERSION, CREATE_TIME, LAST_UPDATED, JOB_CONFIGURATION_LOCATION) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String UPDATE_JOB_EXECUTION = "MODIFY ...";
//            + ""
//            + ""
//            + "UPDATE %PREFIX%JOB_EXECUTION set START_TIME = ?, END_TIME = ?, "
//            + " STATUS = ?, EXIT_CODE = ?, EXIT_MESSAGE = ?, VERSION = ?, CREATE_TIME = ?, LAST_UPDATED = ? where JOB_EXECUTION_ID = ? and VERSION = ?";

    
    
    private static final String SAVE_JOB_EXECUTION = "INSERT into %PREFIX%JOB_EXECUTION(JOB_EXECUTION_ID, JOB_INSTANCE_ID, START_TIME, "
            + "END_TIME, STATUS, EXIT_CODE, EXIT_MESSAGE, VERSION, CREATE_TIME, LAST_UPDATED, JOB_CONFIGURATION_LOCATION) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String CHECK_JOB_EXECUTION_EXISTS = "SELECT COUNT(*) FROM %PREFIX%JOB_EXECUTION WHERE JOB_EXECUTION_ID = ?";

    private static final String GET_STATUS = "SELECT STATUS from %PREFIX%JOB_EXECUTION where JOB_EXECUTION_ID = ?";

//    private static final String UPDATE_JOB_EXECUTION = "UPDATE %PREFIX%JOB_EXECUTION set START_TIME = ?, END_TIME = ?, "
//            + " STATUS = ?, EXIT_CODE = ?, EXIT_MESSAGE = ?, VERSION = ?, CREATE_TIME = ?, LAST_UPDATED = ? where JOB_EXECUTION_ID = ? and VERSION = ?";

    
    
    private static final String FIND_JOB_EXECUTIONS = "SELECT JOB_EXECUTION_ID, START_TIME, END_TIME, STATUS, EXIT_CODE, EXIT_MESSAGE, CREATE_TIME, LAST_UPDATED, VERSION, JOB_CONFIGURATION_LOCATION"
            + " from %PREFIX%JOB_EXECUTION where JOB_INSTANCE_ID = ? order by JOB_EXECUTION_ID desc";

    private static final String GET_LAST_EXECUTION = "SELECT JOB_EXECUTION_ID, START_TIME, END_TIME, STATUS, EXIT_CODE, EXIT_MESSAGE, CREATE_TIME, LAST_UPDATED, VERSION, JOB_CONFIGURATION_LOCATION "
            + "from %PREFIX%JOB_EXECUTION E where JOB_INSTANCE_ID = ? and JOB_EXECUTION_ID in (SELECT max(JOB_EXECUTION_ID) from %PREFIX%JOB_EXECUTION E2 where E2.JOB_INSTANCE_ID = ?)";

    private static final String GET_EXECUTION_BY_ID = "SELECT JOB_EXECUTION_ID, START_TIME, END_TIME, STATUS, EXIT_CODE, EXIT_MESSAGE, CREATE_TIME, LAST_UPDATED, VERSION, JOB_CONFIGURATION_LOCATION"
            + " from %PREFIX%JOB_EXECUTION where JOB_EXECUTION_ID = ?";

    private static final String GET_RUNNING_EXECUTIONS = "SELECT E.JOB_EXECUTION_ID, E.START_TIME, E.END_TIME, E.STATUS, E.EXIT_CODE, E.EXIT_MESSAGE, E.CREATE_TIME, E.LAST_UPDATED, E.VERSION, "
            + "E.JOB_INSTANCE_ID, E.JOB_CONFIGURATION_LOCATION from %PREFIX%JOB_EXECUTION E, %PREFIX%JOB_INSTANCE I where E.JOB_INSTANCE_ID=I.JOB_INSTANCE_ID and I.JOB_NAME=? and E.END_TIME is NULL order by E.JOB_EXECUTION_ID desc";

    private static final String CURRENT_VERSION_JOB_EXECUTION = "SELECT VERSION FROM %PREFIX%JOB_EXECUTION WHERE JOB_EXECUTION_ID=?";

    private static final String FIND_PARAMS_FROM_ID = "SELECT JOB_EXECUTION_ID, KEY_NAME, TYPE_CD, "
            + "STRING_VAL, DATE_VAL, LONG_VAL, DOUBLE_VAL, IDENTIFYING from %PREFIX%JOB_EXECUTION_PARAMS where JOB_EXECUTION_ID = ?";

    private static final String CREATE_JOB_PARAMETERS = "INSERT into %PREFIX%JOB_EXECUTION_PARAMS(JOB_EXECUTION_ID, KEY_NAME, TYPE_CD, "
            + "STRING_VAL, DATE_VAL, LONG_VAL, DOUBLE_VAL, IDENTIFYING) values (?, ?, ?, ?, ?, ?, ?, ?)";

    private int exitMessageLength = DEFAULT_EXIT_MESSAGE_LENGTH;

    private DataFieldMaxValueIncrementer jobExecutionIncrementer;

    /**
     * Public setter for the exit message length in database. Do not set this if
     * you haven't modified the schema.
     * @param exitMessageLength the exitMessageLength to set
     */
    public void setExitMessageLength(int exitMessageLength) {
        this.exitMessageLength = exitMessageLength;
    }

    /**
     * Setter for {@link DataFieldMaxValueIncrementer} to be used when
     * generating primary keys for {@link JobExecution} instances.
     *
     * @param jobExecutionIncrementer the {@link DataFieldMaxValueIncrementer}
     */
    public void setJobExecutionIncrementer(DataFieldMaxValueIncrementer jobExecutionIncrementer) {
        this.jobExecutionIncrementer = jobExecutionIncrementer;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        Assert.notNull(jobExecutionIncrementer, "The jobExecutionIncrementer must not be null.");
    }

    @Override
    public List<JobExecution> findJobExecutions(JobInstance job) {
    	CriteriaQuery<JobExecution> cq = em.getCriteriaBuilder().createQuery(JobExecution.class);
    	TypedQuery<JobExecution> tq = em.createQuery(cq);
    	List<JobExecution> result = tq.getResultList();
    	return result;
    }

    /**
     *
     * SQL implementation using Sequences via the Spring incrementer
     * abstraction. Once a new id has been obtained, the JobExecution is saved
     * via a SQL INSERT statement.
     *
     * @see JobExecutionDao#saveJobExecution(JobExecution)
     * @throws IllegalArgumentException if jobExecution is null, as well as any
     * of it's fields to be persisted.
     */
    @Override
    public void saveJobExecution(JobExecution jobExecution) {
        validateJobExecution(jobExecution);

        jobExecution.incrementVersion();

        em.persist(jobExecution);
    }

    /**
     * Validate JobExecution. At a minimum, JobId, StartTime, EndTime, and
     * Status cannot be null.
     *
     * @param jobExecution
     * @throws IllegalArgumentException
     */
    private void validateJobExecution(JobExecution jobExecution) {

        Assert.notNull(jobExecution);
        Assert.notNull(jobExecution.getJobId(), "JobExecution Job-Id cannot be null.");
        Assert.notNull(jobExecution.getStatus(), "JobExecution status cannot be null.");
        Assert.notNull(jobExecution.getCreateTime(), "JobExecution create time cannot be null");
    }

    /**
     * Update given JobExecution using a SQL UPDATE statement. The JobExecution
     * is first checked to ensure all fields are not null, and that it has an
     * ID. The database is then queried to ensure that the ID exists, which
     * ensures that it is valid.
     *
     * @see JobExecutionDao#updateJobExecution(JobExecution)
     */
    @Override
    public void updateJobExecution(JobExecution jobExecution) {

        validateJobExecution(jobExecution);

        Assert.notNull(jobExecution.getId(),
                "JobExecution ID cannot be null. JobExecution must be saved before it can be updated");

        Assert.notNull(jobExecution.getVersion(),
                "JobExecution version cannot be null. JobExecution must be saved before it can be updated");

        synchronized (jobExecution) {
            Integer version = jobExecution.getVersion() + 1;

            String exitDescription = jobExecution.getExitStatus().getExitDescription();
            if (exitDescription != null && exitDescription.length() > exitMessageLength) {
                exitDescription = exitDescription.substring(0, exitMessageLength);
                if (logger.isDebugEnabled()) {
                    logger.debug("Truncating long message before update of JobExecution: " + jobExecution);
                }
            }
            Object[] parameters = new Object[] { jobExecution.getStartTime(), jobExecution.getEndTime(),
                    jobExecution.getStatus().toString(), jobExecution.getExitStatus().getExitCode(), exitDescription,
                    version, jobExecution.getCreateTime(), jobExecution.getLastUpdated(), jobExecution.getId(),
                    jobExecution.getVersion() };

            // Check if given JobExecution's Id already exists, if none is found
            // it
            // is invalid and
            // an exception should be thrown.
            if (getJdbcTemplate().queryForObject(getQuery(CHECK_JOB_EXECUTION_EXISTS), Integer.class,
                    new Object[] { jobExecution.getId() }) != 1) {
                throw new NoSuchObjectException("Invalid JobExecution, ID " + jobExecution.getId() + " not found.");
            }

            int count = getJdbcTemplate().update(
                    getQuery(UPDATE_JOB_EXECUTION),
                    parameters,
                    new int[] { Types.TIMESTAMP, Types.TIMESTAMP, Types.VARCHAR, Types.VARCHAR, Types.VARCHAR,
                        Types.INTEGER, Types.TIMESTAMP, Types.TIMESTAMP, Types.BIGINT, Types.INTEGER });

            // Avoid concurrent modifications...
            if (count == 0) {
                int curentVersion = getJdbcTemplate().queryForObject(getQuery(CURRENT_VERSION_JOB_EXECUTION), Integer.class,
                        new Object[] { jobExecution.getId() });
                throw new OptimisticLockingFailureException("Attempt to update job execution id="
                        + jobExecution.getId() + " with wrong version (" + jobExecution.getVersion()
                        + "), where current version is " + curentVersion);
            }

            jobExecution.incrementVersion();
        }
    }

    @Override
    public JobExecution getLastJobExecution(JobInstance jobInstance) {
    	
//    	private static final String GET_LAST_EXECUTION = "SELECT JOB_EXECUTION_ID, START_TIME, END_TIME, STATUS, EXIT_CODE, EXIT_MESSAGE, CREATE_TIME, LAST_UPDATED, VERSION, JOB_CONFIGURATION_LOCATION "
//                + "from %PREFIX%JOB_EXECUTION E where JOB_INSTANCE_ID = ? and JOB_EXECUTION_ID in (SELECT max(JOB_EXECUTION_ID) from %PREFIX%JOB_EXECUTION E2 where E2.JOB_INSTANCE_ID = ?)";

    	// WHERE ... AND JOB_EXECUTION_ID in (SELECT max(JOB_EXECUTION_ID) from %PREFIX%JOB_EXECUTION E2 where E2.JOB_INSTANCE_ID = ?)"
        Long id = jobInstance.getId();

    	CriteriaBuilder cb = em.getCriteriaBuilder();
    	CriteriaQuery<JobExecution> q = cb.createQuery(JobExecution.class);

    	//ParameterExpression<Long> p = cb.parameter(Long.class);

    	Root<JobExecution> c = q.from(JobExecution.class);
    	CriteriaQuery<JobExecution> x = q.select(c).where(cb.gt(c.get("jobInstance.id"), id));
    	
    	TypedQuery<JobExecution> query = em.createQuery(x);
    	JobExecution result = query.getSingleResult();
    	
    	return result;
    }

    /*
     * (non-Javadoc)
     *
     * @seeorg.springframework.batch.core.repository.dao.JobExecutionDao#
     * getLastJobExecution(java.lang.String)
     */
    @Override
    public JobExecution getJobExecution(Long executionId) {
        try {
        	CriteriaBuilder cb = em.getCriteriaBuilder();
        	CriteriaQuery<JobExecution> q = cb.createQuery(JobExecution.class);

        	//ParameterExpression<Long> p = cb.parameter(Long.class);

        	Root<JobExecution> c = q.from(JobExecution.class);
        	CriteriaQuery<JobExecution> x = q.select(c).where(cb.gt(c.get("jobInstance.id"), executionId));
        	
        	TypedQuery<JobExecution> query = em.createQuery(x);
        	JobExecution result = query.getSingleResult();
        	
        	return result;
        }
        catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    /*
     * (non-Javadoc)
     *
     * @seeorg.springframework.batch.core.repository.dao.JobExecutionDao#
     * findRunningJobExecutions(java.lang.String)
     */
    @Override
    public Set<JobExecution> findRunningJobExecutions(String jobName) {

        final Set<JobExecution> result = new HashSet<JobExecution>();
        RowCallbackHandler handler = new RowCallbackHandler() {
            @Override
            public void processRow(ResultSet rs) throws SQLException {
                //JobExecutionRowMapper mapper = new JobExecutionRowMapper();
                //result.add(mapper.mapRow(rs, 0));
            }
        };
        getJdbcTemplate().query(getQuery(GET_RUNNING_EXECUTIONS), new Object[] { jobName }, handler);

        return result;
    }

    @Override
    public void synchronizeStatus(JobExecution jobExecution) {
        int currentVersion = getJdbcTemplate().queryForObject(getQuery(CURRENT_VERSION_JOB_EXECUTION), Integer.class,
                jobExecution.getId());

        if (currentVersion != jobExecution.getVersion().intValue()) {
            String status = getJdbcTemplate().queryForObject(getQuery(GET_STATUS), String.class, jobExecution.getId());
            jobExecution.upgradeStatus(BatchStatus.valueOf(status));
            jobExecution.setVersion(currentVersion);
        }
    }

    /**
     * Convenience method that inserts all parameters from the provided
     * JobParameters.
     *
     */
    private void insertJobParameters(Resource jobParamsRes, Long executionId, JobParameters jobParameters) {

        jobParamsRes.addLiteral(BATCH.jobExecutionId, executionId);

        for (Entry<String, JobParameter> entry : jobParameters.getParameters().entrySet()) {
            Resource jobParamRes = jobParamsRes.getModel().createResource();

            jobParamsRes.addProperty(BATCH.hasParam, jobParamRes);

            String key = entry.getKey();

            JobParameter jobParameter = entry.getValue();
            insertParameter(jobParamRes,executionId, jobParameter.getType(), entry.getKey(),
                  jobParameter.getValue(), jobParameter.isIdentifying());
        }


//        for (Entry<String, JobParameter> entry : jobParameters.getParameters()
//                .entrySet()) {
//            JobParameter jobParameter = entry.getValue();
//            insertParameter(executionId, jobParameter.getType(), entry.getKey(),
//                    jobParameter.getValue(), jobParameter.isIdentifying());
//        }
    }

    /**
     * Convenience method that inserts an individual records into the
     * JobParameters table.
     */
    private void insertParameter(Resource jobParamRes, Long executionId, ParameterType type, String key,
            Object value, boolean identifying) {

        jobParamRes
            .addLiteral(BATCH.jobExecutionId, executionId)
            .addLiteral(BATCH.key, key)
            .addLiteral(BATCH.value, value)
            .addLiteral(BATCH.identifying, identifying);


//        Object[] args = new Object[0];
//        int[] argTypes = new int[] { Types.BIGINT, Types.VARCHAR,
//                Types.VARCHAR, Types.VARCHAR, Types.TIMESTAMP, Types.BIGINT,
//                Types.DOUBLE, Types.CHAR };
//
//        String identifyingFlag = identifying? "Y":"N";
//
//        if (type == ParameterType.STRING) {
//            args = new Object[] { executionId, key, type, value, new Timestamp(0L),
//                    0L, 0D, identifyingFlag};
//        } else if (type == ParameterType.LONG) {
//            args = new Object[] { executionId, key, type, "", new Timestamp(0L),
//                    value, new Double(0), identifyingFlag};
//        } else if (type == ParameterType.DOUBLE) {
//            args = new Object[] { executionId, key, type, "", new Timestamp(0L), 0L,
//                    value, identifyingFlag};
//        } else if (type == ParameterType.DATE) {
//            args = new Object[] { executionId, key, type, "", value, 0L, 0D, identifyingFlag};
//        }
//
//        getJdbcTemplate().update(getQuery(CREATE_JOB_PARAMETERS), args, argTypes);
    }

    /**
     * @param executionId
     * @return
     */
    protected JobParameters getJobParameters(Long executionId) {
        final Map<String, JobParameter> map = new HashMap<String, JobParameter>();
        RowCallbackHandler handler = new RowCallbackHandler() {
            @Override
            public void processRow(ResultSet rs) throws SQLException {
                ParameterType type = ParameterType.valueOf(rs.getString(3));
                JobParameter value = null;

                if (type == ParameterType.STRING) {
                    value = new JobParameter(rs.getString(4), rs.getString(8).equalsIgnoreCase("Y"));
                } else if (type == ParameterType.LONG) {
                    value = new JobParameter(rs.getLong(6), rs.getString(8).equalsIgnoreCase("Y"));
                } else if (type == ParameterType.DOUBLE) {
                    value = new JobParameter(rs.getDouble(7), rs.getString(8).equalsIgnoreCase("Y"));
                } else if (type == ParameterType.DATE) {
                    value = new JobParameter(rs.getTimestamp(5), rs.getString(8).equalsIgnoreCase("Y"));
                }

                // No need to assert that value is not null because it's an enum
                map.put(rs.getString(2), value);
            }
        };

        getJdbcTemplate().query(getQuery(FIND_PARAMS_FROM_ID), new Object[] { executionId }, handler);

        return new JobParameters(map);
    }


//
//    /**
//     * Re-usable mapper for {@link JobExecution} instances.
//     *
//     * @author Dave Syer
//     *
//     */
//    private final class JobExecutionRowMapper implements RowMapper<JobExecution> {
//
//        private JobInstance jobInstance;
//
//        private JobParameters jobParameters;
//
//        public JobExecutionRowMapper() {
//        }
//
//        public JobExecutionRowMapper(JobInstance jobInstance) {
//            this.jobInstance = jobInstance;
//        }
//
//        @Override
//        public JobExecution mapRow(ResultSet rs, int rowNum) throws SQLException {
//            Long id = rs.getLong(1);
//            String jobConfigurationLocation = rs.getString(10);
//            JobExecution jobExecution;
//            if (jobParameters == null) {
//                jobParameters = getJobParameters(id);
//            }
//
//            if (jobInstance == null) {
//                jobExecution = new JobExecution(id, jobParameters, jobConfigurationLocation);
//            }
//            else {
//                jobExecution = new JobExecution(jobInstance, id, jobParameters, jobConfigurationLocation);
//            }
//
//            jobExecution.setStartTime(rs.getTimestamp(2));
//            jobExecution.setEndTime(rs.getTimestamp(3));
//            jobExecution.setStatus(BatchStatus.valueOf(rs.getString(4)));
//            jobExecution.setExitStatus(new ExitStatus(rs.getString(5), rs.getString(6)));
//            jobExecution.setCreateTime(rs.getTimestamp(7));
//            jobExecution.setLastUpdated(rs.getTimestamp(8));
//            jobExecution.setVersion(rs.getInt(9));
//            return jobExecution;
//        }
//
//    }
}
