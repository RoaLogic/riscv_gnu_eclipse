/*******************************************************************************
* This program and the accompanying materials
* are made available under the terms of the Common Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/cpl-v10.html
* 
* Contributors:
*     RoaLogic BV - RISC-V Gnu Toolchain port
*******************************************************************************/

package org.eclipse.cdt.cross.riscv.gnu;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Properties;

import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.cdt.managedbuilder.core.IManagedCommandLineInfo;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.cdt.managedbuilder.internal.core.ManagedCommandLineGenerator;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.variables.VariablesPlugin;
import org.eclipse.ui.statushandlers.StatusManager;

public class RISCVManagedCommandLineGenerator extends ManagedCommandLineGenerator {
	
    public IManagedCommandLineInfo generateCommandLineInfo(ITool oTool, String sCommandName,
            String[] asFlags, String sOutputFlag, String sOutputPrefix, String sOutputName,
            String[] asInputResources, String sCommandLinePattern) {
        return generateCommandLineInfo(oTool, sCommandName, asFlags, sOutputFlag, sOutputPrefix,
                sOutputName, asInputResources, sCommandLinePattern, false);
    }

    
    public IManagedCommandLineInfo generateCommandLineInfo(ITool oTool, String sCommandName, String[] asFlags, String sOutputFlag, String sOutputPrefix, String sOutputName, String[] asInputResources, String sCommandLinePattern, boolean bFlag)
    {
      ArrayList<String> oList = new ArrayList<String>();
      ArrayList<String> oList_gcc_options = new ArrayList<String>();

      //RISC-V Options
      String  sProcessor = null;
      boolean hasRVA     = false;
      boolean hasRVC     = false;
      boolean hasRVM     = false;
      boolean hasFPU     = false;
      boolean bFDIV      = false;
      String  sArch      = "";
   
      //Get Tool options
      IOption[] oToolOptions = oTool.getOptions();
      for (int i = 0; i < oToolOptions.length; i++) {
          IOption oOption = oToolOptions[i];
          String sID = oOption.getId();
   
          Object oValue = oOption.getValue();
   
          String sCommand = oOption.getCommand();
          if ((oValue instanceof String)) {
             String sVal;
             try {
               sVal = oOption.getStringValue();
             }
             catch (BuildException e) {
               sVal = null;
             }
             
             String sEnumCommand;
             try {
               sEnumCommand = oOption.getEnumCommand(sVal);
             }
             catch (BuildException e1) {
               sEnumCommand = null;
             }
   
             //Insert string analysis here
          }
          else if ((oValue instanceof Boolean)) {
            boolean bVal;
            try {
              bVal = oOption.getBooleanValue();
            }
            catch (BuildException e) {
             bVal = false;
            }

            if (bVal) {   
            	if (sID.indexOf(".option.misc.fdiv") > 0) {
                    bFDIV = true;
                }
            }
          }
      } //next i
    
      
      //Get ToolChain options
      Object oParent = oTool.getParent();
      while ((oParent != null) && (!(oParent instanceof IToolChain))) {
        Object oSuper = oTool.getSuperClass();
        if ((oSuper != null) && ((oSuper instanceof ITool)))
          oParent = ((ITool)oSuper).getParent();
        else {
          oParent = null;
        }
      }
     
      if ((oParent != null) && ((oParent instanceof IToolChain))) {
        IToolChain oToolChain = (IToolChain)oParent;
        IOption[] aoOptions = oToolChain.getOptions();
   
        String sProcessorEndiannes = null;
   
        String sSyntaxonly = null;   
        String sDebugLevel = null;   
        String sDebugFormat = null;   
        String sDebugOther = null;   
        String sDebugProf = null;   
        String sDebugGProf = null;


        for (int i = 0; i < aoOptions.length; i++) {
          IOption oOption = aoOptions[i];
          String sID = oOption.getId();
   
          Object oValue = oOption.getValue();
   
          String sCommand = oOption.getCommand();
          if ((oValue instanceof String)) {
             String sVal;
             try {
               sVal = oOption.getStringValue();
             }
             catch (BuildException e) {
               // yunluz comment String sVal;
               sVal = null;
             }
             String sEnumCommand;
             try {
               sEnumCommand = oOption.getEnumCommand(sVal);
             }
             catch (BuildException e1) {
               //yunluz String sEnumCommand;
               sEnumCommand = null;
             }

             if (sID.indexOf(".option.target.processor") > 0) {
               sProcessor = sEnumCommand;
             } else if (sID.indexOf(".option.target.endiannes") > 0) {
               sProcessorEndiannes = sEnumCommand;
             } else if (sID.indexOf(".option.warnings.syntax") > 0) {
               sSyntaxonly = sEnumCommand;
             } else if (sID.indexOf(".option.debugging.level") > 0) {
               sDebugLevel = sEnumCommand;
             } else if (sID.indexOf(".option.debugging.format") > 0) {
               sDebugFormat = sEnumCommand;
             } else if (sID.indexOf(".option.debugging.other") > 0) {
               sDebugOther = sVal;
             } 
          }
          else if ((oValue instanceof Boolean)) {
            boolean bVal;
            try {
              bVal = oOption.getBooleanValue();
            }
            catch (BuildException e) {
             //yunluz boolean bVal;
             bVal = false;
            }

            if (bVal) {   
                if (sID.indexOf(".option.debugging.prof") > 0) {
                    sDebugProf = sCommand;
                } else if (sID.indexOf(".option.target.arch.rva") > 0 ) {
                    sArch = sArch.concat("A");
                    hasRVA = true;
                } else if (sID.indexOf(".option.target.arch.rvc") > 0 ) {
                    sArch = sArch.concat("C");
                    hasRVC = true;
                } else if (sID.indexOf(".option.target.arch.rve") > 0 ) {
                    sArch = sArch.concat("E");
                } else if (sID.indexOf(".option.target.arch.rvm") > 0 ) {
                    sArch = sArch.concat("M");
                    hasRVM = true;
                } else if (sID.indexOf(".option.target.arch.rvf") > 0 ) {
                    sArch = sArch.concat("F");
                    hasFPU = true;
                } else if (sID.indexOf(".option.target.arch.rvd") > 0 ) {
                    sArch = sArch.concat("D");
                } else if (sID.indexOf(".option.target.arch.rvq") > 0 ) {
                    sArch = sArch.concat("Q");
                } else if (sID.indexOf(".option.debugging.gprof") > 0) {
                    sDebugGProf = sCommand;
                }
            }
          }
        } //next i
        
        if (sProcessorEndiannes != null && !sProcessorEndiannes.isEmpty())
            oList_gcc_options.add(sProcessorEndiannes);
        if (sSyntaxonly != null && !sSyntaxonly.isEmpty())
            oList.add(sSyntaxonly);
        if (sDebugLevel != null && !sDebugLevel.isEmpty()) {
            oList.add(sDebugLevel);
            if (sDebugFormat != null && !sDebugFormat.isEmpty())
              oList.add(sDebugFormat);
        }
        if (sDebugOther != null && !sDebugOther.isEmpty())
            oList.add(sDebugOther);
        if (sDebugProf != null && !sDebugProf.isEmpty())
            oList.add(sDebugProf);
        if (sDebugGProf != null && !sDebugGProf.isEmpty())
            oList.add(sDebugGProf);
      }
      
      
      //Create RISC-V command line arguments
      if (sProcessor != null && !sProcessor.isEmpty()) {
         oList.add(sProcessor);
         if (sProcessor.equals("-m64")) {
           sArch = "-march=RV64I" + sArch;
         } else {
           sArch = "-march=RV32I" + sArch;
         }
      }
      oList_gcc_options.add(sArch);

      if (hasRVA)
         oList_gcc_options.add("-matomics");
      if (hasRVC)
         oList_gcc_options.add("-mrvc");
      if (hasRVM)
    	  oList_gcc_options.add("-mmuldiv");
      if (hasFPU) {
    	 oList_gcc_options.add("-mhard-float");
    	 if (bFDIV)
    		 oList_gcc_options.add("-mfdiv");
      } else {
    	 oList_gcc_options.add("-msoft-float");
      }

      oList.addAll(Arrays.asList(asFlags));
      oList.addAll(oList_gcc_options);

      return super.generateCommandLineInfo(oTool, sCommandName,
                (String[]) oList.toArray(new String[0]), sOutputFlag, sOutputPrefix, sOutputName,
                asInputResources, sCommandLinePattern);
    }
    
}

