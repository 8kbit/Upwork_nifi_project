package com.thoughtapps.droppoint.core.dto;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

import java.util.List;

/**
 * Created by zaskanov on 01.04.2017.
 */

/**
 * Used to transfer, marshal and unmarshal instruction JSON
 */
@Data
public class Instruction {
    @SerializedName("sftp.server.rule")
    InstructionType type;
    @SerializedName("sftp.server.rule.name")
    String ruleName;
    @SerializedName("sftp.server.filter.dir.path")
    String filterDirPath;
    @SerializedName("sftp.server.filter.filename")
    String filterFilename;
    @SerializedName("sftp.server.filter.ignore.dotted.files")
    Boolean filterIsIgnoreDottedFiles = false;
    @SerializedName("sftp.server.filter.file.includefileregexlist")
    List<String> filterFileIncludeFileRegexList;
    @SerializedName("sftp.server.filter.file.excludefileregexlist")
    List<String> filterFileExcludeFileRegexList;
    @SerializedName("sftp.server.filter.filetype")
    String filterFileType;
    @SerializedName("sftp.server.filter.filecontent")
    String filterFileContent;
    @SerializedName("sftp.server.filter.files.per.batch")
    Integer filterFilesPerBatch;
    @SerializedName("sftp.server.filter.total.files")
    Integer filterTotalFiles;
    @SerializedName("sftp.server.filter.delete.original")
    Boolean filterIsDeleteOriginal = false;
    @SerializedName("sftp.server.filter.ignore.processed.file")
    Boolean filterIsIgnoreProcessedFile = false;
    @SerializedName("sftp.server.polling.interval")
    Integer poolingIntervalSec;
    @SerializedName("sftp.server.push.recursively")
    Boolean isPushRecursively = false;
    @SerializedName("sftp.server.filter.recursion.depth")
    Integer filterRecursionDepth;
    @SerializedName("sftp.server.use.compression")
    Boolean isUseCompression = false;
    @SerializedName("sftp.server.use.natural.ordering")
    Boolean isUseNaturalOrdering = false;
    @SerializedName("sftp.server.database.connection.url")
    String dbConnectionURL;
    @SerializedName("sftp.server.database.connection.driver.class.name")
    String dbConnectionDriverClassName;
    @SerializedName("sftp.server.database.connection.driver.location(s)")
    List<String> dbConnectionDriverLocations;
    @SerializedName("sftp.server.database.user")
    String dbUser;
    @SerializedName("sftp.server.database.password")
    String dbPassword;
    @SerializedName("sftp.server.database.sql.query")
    String dbSQLQuery;
}
