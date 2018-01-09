package com.infoclinika.mssharing.model.internal.entity;

import com.infoclinika.mssharing.model.api.*;
import org.hibernate.annotations.Index;

import javax.persistence.*;

/**
 * @author Oleksii Tymchenko
 */
@Entity
public class MSFunctionItem extends AbstractAggregate {
    private static final int HASH_MULTIPLIER = 31;

    private String functionName;
    @Column(length = 1000)
    @Index(name = "TRANSLATED_PATH_IDX")
    private String translatedPath;

    @Enumerated(EnumType.STRING)
    private MSFunctionDataType dataType = MSFunctionDataType.PROFILE;
    @Enumerated(EnumType.STRING)
    private MSFunctionImageType imageType = MSFunctionImageType.SHORT_IMAGE;
    @Enumerated(EnumType.STRING)
    private MSFunctionType functionType = MSFunctionType.MS;
    @Enumerated(EnumType.STRING)
    private MSFunctionFragmentationType fragmentationType = MSFunctionFragmentationType.CAD;
    @Enumerated(EnumType.STRING)
    private MSFunctionScanType scanType = MSFunctionScanType.FULL;
    @Enumerated(EnumType.STRING)
    private MSFunctionMassAnalyzerType massAnalyzerType = MSFunctionMassAnalyzerType.FTMS;
    @Enumerated(EnumType.STRING)
    private MSResolutionType resolutionType;

    private String instrumentModel;
    private String instrumentName;
    private boolean dia = false;
    private boolean polarity = false;
    private boolean calibration = false;
    private int resolution;
    private int functionNumber;
    private int maxScan = -1;
    private int maxPackets = -1;
    //low mz and high mz are absent for the already translated files; so make the fields nullable
    private Integer lowMz;
    private Integer highMz;

    public String getFunctionName() {
        return functionName;
    }

    public void setFunctionName(String functionName) {
        this.functionName = functionName;
    }

    public String getTranslatedPath() {
        return translatedPath;
    }

    public void setTranslatedPath(String translatedPath) {
        this.translatedPath = translatedPath;
    }

    public MSFunctionDataType getDataType() {
        return dataType;
    }

    public void setDataType(MSFunctionDataType dataType) {
        this.dataType = dataType;
    }

    public MSFunctionImageType getImageType() {
        return imageType;
    }

    public void setImageType(MSFunctionImageType imageType) {
        this.imageType = imageType;
    }

    public MSFunctionType getFunctionType() {
        return functionType;
    }

    public void setFunctionType(MSFunctionType functionType) {
        this.functionType = functionType;
    }

    public int getResolution() {
        return resolution;
    }

    public void setResolution(int resolution) {
        this.resolution = resolution;
    }


    public boolean isDia() {
        return dia;
    }

    public void setDia(boolean dia) {
        this.dia = dia;
    }

    public boolean isPolarity() {
        return polarity;
    }

    public void setPolarity(boolean polarity) {
        this.polarity = polarity;
    }


    public boolean isCalibration() {
        return calibration;
    }

    public void setCalibration(boolean calibration) {
        this.calibration = calibration;
    }

    public String getInstrumentModel() {
        return instrumentModel;
    }

    public void setInstrumentModel(String instrumentModel) {
        this.instrumentModel = instrumentModel;
    }

    public String getInstrumentName() {
        return instrumentName;
    }

    public void setInstrumentName(String instrumentName) {
        this.instrumentName = instrumentName;
    }

    public int getFunctionNumber() {
        return functionNumber;
    }

    public void setFunctionNumber(int functionNumber) {
        this.functionNumber = functionNumber;
    }

    public int getMaxScan() {
        return maxScan;
    }

    public void setMaxScan(int maxScan) {
        this.maxScan = maxScan;
    }

    public int getMaxPackets() {
        return maxPackets;
    }

    public void setMaxPackets(int maxPackets) {
        this.maxPackets = maxPackets;
    }

    public MSFunctionFragmentationType getFragmentationType() {
        return fragmentationType;
    }

    public void setFragmentationType(MSFunctionFragmentationType fragmentationType) {
        this.fragmentationType = fragmentationType;
    }

    public MSFunctionScanType getScanType() {
        return scanType;
    }

    public void setScanType(MSFunctionScanType scanType) {
        this.scanType = scanType;
    }

    public MSFunctionMassAnalyzerType getMassAnalyzerType() {
        return massAnalyzerType;
    }

    public void setMassAnalyzerType(MSFunctionMassAnalyzerType massAnalyzerType) {
        this.massAnalyzerType = massAnalyzerType;
    }

    public MSResolutionType getResolutionType() {
        return resolutionType;
    }

    public void setResolutionType(MSResolutionType resolutionType) {
        this.resolutionType = resolutionType;
    }

    public Integer getLowMz() {
        return lowMz;
    }

    public void setLowMz(Integer lowMz) {
        this.lowMz = lowMz;
    }

    public Integer getHighMz() {
        return highMz;
    }

    public void setHighMz(Integer highMz) {
        this.highMz = highMz;
    }


    public MSFunctionItem copy() {
        MSFunctionItem copyFunction = new MSFunctionItem();
        copyFunction.setCalibration(this.isCalibration());
        copyFunction.setFragmentationType(this.getFragmentationType());
        copyFunction.setDataType(this.getDataType());
        copyFunction.setDia(this.isDia());
        copyFunction.setScanType(this.getScanType());
        copyFunction.setMassAnalyzerType(this.getMassAnalyzerType());
        copyFunction.setResolutionType(this.getResolutionType());
        copyFunction.setFunctionName(this.getFunctionName());
        copyFunction.setFunctionNumber(this.getFunctionNumber());
        copyFunction.setFunctionType(this.getFunctionType());
        copyFunction.setImageType(this.getImageType());
        copyFunction.setInstrumentModel(this.getInstrumentModel());
        copyFunction.setInstrumentName(this.getInstrumentName());
        copyFunction.setMaxPackets(this.getMaxPackets());
        copyFunction.setMaxScan(this.getMaxScan());
        copyFunction.setPolarity(this.isPolarity());
        copyFunction.setResolution(this.getResolution());
        copyFunction.setTranslatedPath(this.getTranslatedPath());
        copyFunction.setLowMz(this.getLowMz());
        copyFunction.setHighMz(this.getHighMz());
        return copyFunction;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        MSFunctionItem that = (MSFunctionItem) o;

        if (calibration != that.calibration) {
            return false;
        }
        if (dia != that.dia) {
            return false;
        }
        if (functionNumber != that.functionNumber) {
            return false;
        }
        if (maxPackets != that.maxPackets) {
            return false;
        }
        if (maxScan != that.maxScan) {
            return false;
        }
        if (polarity != that.polarity) {
            return false;
        }
        if (resolution != that.resolution) {
            return false;
        }
        if (dataType != that.dataType) {
            return false;
        }
        if (fragmentationType != that.fragmentationType) {
            return false;
        }
        if (functionName != null ? !functionName.equals(that.functionName) : that.functionName != null) {
            return false;
        }
        if (functionType != that.functionType) {
            return false;
        }
        if (highMz != null ? !highMz.equals(that.highMz) : that.highMz != null) {
            return false;
        }
        if (imageType != that.imageType) {
            return false;
        }
        if (instrumentModel != null ? !instrumentModel.equals(that.instrumentModel) : that.instrumentModel != null) {
            return false;
        }
        if (instrumentName != null ? !instrumentName.equals(that.instrumentName) : that.instrumentName != null) {
            return false;
        }
        if (lowMz != null ? !lowMz.equals(that.lowMz) : that.lowMz != null) {
            return false;
        }
        if (massAnalyzerType != that.massAnalyzerType) {
            return false;
        }
        if (resolutionType != that.resolutionType) {
            return false;
        }
        if (scanType != that.scanType) {
            return false;
        }
        if (translatedPath != null ? !translatedPath.equals(that.translatedPath) : that.translatedPath != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = HASH_MULTIPLIER * result + (functionName != null ? functionName.hashCode() : 0);
        result = HASH_MULTIPLIER * result + (translatedPath != null ? translatedPath.hashCode() : 0);
        result = HASH_MULTIPLIER * result + (dataType != null ? dataType.hashCode() : 0);
        result = HASH_MULTIPLIER * result + (imageType != null ? imageType.hashCode() : 0);
        result = HASH_MULTIPLIER * result + (functionType != null ? functionType.hashCode() : 0);
        result = HASH_MULTIPLIER * result + (fragmentationType != null ? fragmentationType.hashCode() : 0);
        result = HASH_MULTIPLIER * result + (scanType != null ? scanType.hashCode() : 0);
        result = HASH_MULTIPLIER * result + (massAnalyzerType != null ? massAnalyzerType.hashCode() : 0);
        result = HASH_MULTIPLIER * result + (resolutionType != null ? resolutionType.hashCode() : 0);
        result = HASH_MULTIPLIER * result + (instrumentModel != null ? instrumentModel.hashCode() : 0);
        result = HASH_MULTIPLIER * result + (instrumentName != null ? instrumentName.hashCode() : 0);
        result = HASH_MULTIPLIER * result + (dia ? 1 : 0);
        result = HASH_MULTIPLIER * result + (polarity ? 1 : 0);
        result = HASH_MULTIPLIER * result + (calibration ? 1 : 0);
        result = HASH_MULTIPLIER * result + resolution;
        result = HASH_MULTIPLIER * result + functionNumber;
        result = HASH_MULTIPLIER * result + maxScan;
        result = HASH_MULTIPLIER * result + maxPackets;
        result = HASH_MULTIPLIER * result + (lowMz != null ? lowMz.hashCode() : 0);
        result = HASH_MULTIPLIER * result + (highMz != null ? highMz.hashCode() : 0);

        return result;
    }

    @Override
    public String toString() {
        return "MSFunctionItem{" +
                "functionName='" + functionName + '\'' +
                ", translatedPath='" + translatedPath + '\'' +
                ", dataType=" + dataType +
                ", imageType=" + imageType +
                ", functionType=" + functionType +
                ", fragmentationType=" + fragmentationType +
                ", scanType=" + scanType +
                ", massAnalyzerType=" + massAnalyzerType +
                ", resolutionType=" + resolutionType +
                ", instrumentModel='" + instrumentModel + '\'' +
                ", instrumentName='" + instrumentName + '\'' +
                ", dia=" + dia +
                ", polarity=" + polarity +
                ", calibration=" + calibration +
                ", resolution=" + resolution +
                ", functionNumber=" + functionNumber +
                ", maxScan=" + maxScan +
                ", maxPackets=" + maxPackets +
                ", lowMz=" + lowMz +
                ", highMz=" + highMz +
                "} " + super.toString();
    }
}
