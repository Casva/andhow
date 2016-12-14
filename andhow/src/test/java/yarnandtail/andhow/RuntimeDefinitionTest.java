package yarnandtail.andhow;

import static org.junit.Assert.*;
import org.junit.Test;
import yarnandtail.andhow.internal.RuntimeDefinition;
import yarnandtail.andhow.name.AsIsAliasNamingStrategy;
import yarnandtail.andhow.name.BasicNamingStrategy;
import yarnandtail.andhow.property.StrProp;

/**
 *
 * @author eeverman
 */
public class RuntimeDefinitionTest {
	
	String paramFullPath = SimpleParamsWAlias.class.getCanonicalName() + ".";
	
	@Test
	public void testHappyPath() {
		
		NamingStrategy bns = new BasicNamingStrategy();
		
		RuntimeDefinition appDef = new RuntimeDefinition();
		appDef.addProperty(SimpleParamsWAlias.class, SimpleParamsWAlias.KVP_BOB, 
				bns.buildNames(SimpleParamsWAlias.KVP_BOB, SimpleParamsWAlias.class, "KVP_BOB"));
		appDef.addProperty(SimpleParamsWAlias.class, SimpleParamsWAlias.FLAG_FALSE, 
				bns.buildNames(SimpleParamsWAlias.FLAG_FALSE, SimpleParamsWAlias.class, "FLAG_FALSE"));

		//Canonical Names for Point
		assertEquals(paramFullPath + "KVP_BOB", appDef.getCanonicalName(SimpleParamsWAlias.KVP_BOB));
		assertEquals(paramFullPath + "FLAG_FALSE", appDef.getCanonicalName(SimpleParamsWAlias.FLAG_FALSE));
		
		//Get points for Canonical name and alias
		assertEquals(SimpleParamsWAlias.KVP_BOB, appDef.getProperty(paramFullPath + "KVP_BOB"));
		assertEquals(SimpleParamsWAlias.FLAG_FALSE, appDef.getProperty(paramFullPath + "FLAG_FALSE"));
		assertEquals(SimpleParamsWAlias.KVP_BOB, appDef.getProperty(paramFullPath + 
				SimpleParamsWAlias.KVP_BOB.getBaseAliases().get(0)));
		assertEquals(SimpleParamsWAlias.FLAG_FALSE, appDef.getProperty(paramFullPath + 
				SimpleParamsWAlias.FLAG_FALSE.getBaseAliases().get(0)));
		
		//Groups
		assertEquals(1, appDef.getPropertyGroups().size());
		assertEquals(SimpleParamsWAlias.class, appDef.getPropertyGroups().get(0));
		
		//Point list
		assertEquals(2, appDef.getProperties().size());
		assertEquals(SimpleParamsWAlias.KVP_BOB, appDef.getProperties().get(0));
		assertEquals(SimpleParamsWAlias.FLAG_FALSE, appDef.getProperties().get(1));
		
		//Points for Group
		assertEquals(2, appDef.getPropertiesForGroup(SimpleParamsWAlias.class).size());
		assertEquals(SimpleParamsWAlias.KVP_BOB, appDef.getPropertiesForGroup(SimpleParamsWAlias.class).get(0));
		assertEquals(SimpleParamsWAlias.FLAG_FALSE, appDef.getPropertiesForGroup(SimpleParamsWAlias.class).get(1));
		assertEquals(0, appDef.getPropertiesForGroup(SimpleParamsNoAlias.class).size());		//A random group that is not registered 
	}
	
	@Test
	public void testDuplicatePointInSeparateGroupWithDistinctNames() {
		
		NamingStrategy bns = new BasicNamingStrategy();
		
		RuntimeDefinition appDef = new RuntimeDefinition();
		appDef.addProperty(SimpleParamsWAlias.class, SimpleParamsWAlias.KVP_BOB, 
				bns.buildNames(SimpleParamsWAlias.KVP_BOB, SimpleParamsWAlias.class, "KVP_BOB"));
		appDef.addProperty(SimpleParamsNoAlias.class, SimpleParamsWAlias.KVP_BOB, 
				bns.buildNames(SimpleParamsWAlias.FLAG_FALSE, SimpleParamsWAlias.class, "fakeName"));
		
		assertEquals(1, appDef.getProperties().size());
		assertEquals(SimpleParamsWAlias.KVP_BOB, appDef.getProperties().get(0));
		assertEquals(1, appDef.getConstructionProblems().size());
		assertTrue(appDef.getConstructionProblems().get(0) instanceof ConstructionProblem.DuplicateProperty);
		
		ConstructionProblem.DuplicateProperty dpcp = (ConstructionProblem.DuplicateProperty)appDef.getConstructionProblems().get(0);
		
		assertEquals(SimpleParamsWAlias.KVP_BOB, dpcp.getRefProperty().getProperty());
		assertEquals(SimpleParamsWAlias.class, dpcp.getRefProperty().getGroup());
		assertEquals(bns.buildNames(SimpleParamsWAlias.KVP_BOB, SimpleParamsWAlias.class, "KVP_BOB").getCanonicalName(), dpcp.getRefProperty().getName());
		assertEquals(SimpleParamsWAlias.KVP_BOB, dpcp.getBadProperty().getProperty());
		assertEquals(SimpleParamsNoAlias.class, dpcp.getBadProperty().getGroup());
		assertEquals(bns.buildNames(SimpleParamsWAlias.KVP_BOB, SimpleParamsWAlias.class, "fakeName").getCanonicalName(), dpcp.getBadProperty().getName());
	}
	
	@Test
	public void testDuplicateAlias() {
		String dupParamFullPath = SimpleParamsWAliasDuplicate.class.getCanonicalName() + ".";
		
		//Use Aliases as-is to cause naming collisions
		NamingStrategy bns = new AsIsAliasNamingStrategy();
		RuntimeDefinition appDef = new RuntimeDefinition();
		
		appDef.addProperty(SimpleParamsWAlias.class, SimpleParamsWAlias.KVP_BOB, 
				bns.buildNames(SimpleParamsWAlias.KVP_BOB, SimpleParamsWAlias.class, "KVP_BOB"));
		appDef.addProperty(SimpleParamsWAlias.class, SimpleParamsWAlias.FLAG_FALSE, 
				bns.buildNames(SimpleParamsWAlias.FLAG_FALSE, SimpleParamsWAlias.class, "FLAG_FALSE"));
		
		// THIS ONE WILL HAVE A DUPLICATE NAME
		appDef.addProperty(SimpleParamsWAliasDuplicate.class, SimpleParamsWAliasDuplicate.FLAG_FALSE, 
				bns.buildNames(SimpleParamsWAliasDuplicate.FLAG_FALSE, SimpleParamsWAliasDuplicate.class, "FLAG_FALSE"));
		// DUPLICATE
		
		//Continue w/ a non-duplicate, which should be added as normal
		appDef.addProperty(SimpleParamsWAliasDuplicate.class, SimpleParamsWAliasDuplicate.FLAG_TRUE, 
				bns.buildNames(SimpleParamsWAliasDuplicate.FLAG_TRUE, SimpleParamsWAliasDuplicate.class, "FLAG_TRUE"));

		//Check values that were actually added to list - The dup point should not have been added
		assertEquals(3, appDef.getProperties().size());
		assertEquals(SimpleParamsWAlias.KVP_BOB, appDef.getProperties().get(0));
		assertEquals(SimpleParamsWAlias.FLAG_FALSE, appDef.getProperties().get(1));
		assertEquals(SimpleParamsWAliasDuplicate.FLAG_TRUE, appDef.getProperties().get(2));
		
		//We should have created 1 NonUniuqeName problem
		assertEquals(1, appDef.getConstructionProblems().size());
		assertTrue(appDef.getConstructionProblems().get(0) instanceof ConstructionProblem.NonUniqueNames);
		ConstructionProblem.NonUniqueNames dupCpn = (ConstructionProblem.NonUniqueNames)appDef.getConstructionProblems().get(0);
		
		//Check problem specifics
		assertEquals(SimpleParamsWAliasDuplicate.FLAG_FALSE, dupCpn.getBadProperty().getProperty());
		assertEquals(dupParamFullPath + "FLAG_FALSE", dupCpn.getBadProperty().getName());
		assertEquals(SimpleParamsWAliasDuplicate.FLAG_FALSE.getBaseAliases().get(0), dupCpn.getConflictName());
		assertEquals(SimpleParamsWAlias.FLAG_FALSE, dupCpn.getRefProperty().getProperty());
		assertEquals(paramFullPath + "FLAG_FALSE", dupCpn.getRefProperty().getName());
		
		//Canonical Names for Point
		assertEquals(paramFullPath + "KVP_BOB", appDef.getCanonicalName(SimpleParamsWAlias.KVP_BOB));
		assertEquals(paramFullPath + "FLAG_FALSE", appDef.getCanonicalName(SimpleParamsWAlias.FLAG_FALSE));
		assertNull(appDef.getCanonicalName(SimpleParamsWAliasDuplicate.FLAG_FALSE));		//not added b/c dup
		assertEquals(dupParamFullPath + "FLAG_TRUE", appDef.getCanonicalName(SimpleParamsWAliasDuplicate.FLAG_TRUE));
		
		//Get points for Canonical name and alias
		assertEquals(SimpleParamsWAlias.KVP_BOB, appDef.getProperty(paramFullPath + "KVP_BOB"));
		assertEquals(SimpleParamsWAlias.KVP_BOB, appDef.getProperty(SimpleParamsWAlias.KVP_BOB.getBaseAliases().get(0)));
		assertEquals(SimpleParamsWAlias.FLAG_FALSE, appDef.getProperty(paramFullPath + "FLAG_FALSE"));
		assertEquals(SimpleParamsWAlias.FLAG_FALSE, appDef.getProperty(SimpleParamsWAlias.FLAG_FALSE.getBaseAliases().get(0)));
		assertEquals(SimpleParamsWAliasDuplicate.FLAG_TRUE, appDef.getProperty(dupParamFullPath + "FLAG_TRUE"));
		assertEquals(SimpleParamsWAliasDuplicate.FLAG_TRUE, appDef.getProperty(SimpleParamsWAliasDuplicate.FLAG_TRUE.getBaseAliases().get(0)));
		
		//Groups
		assertEquals(2, appDef.getPropertyGroups().size());
		assertEquals(SimpleParamsWAlias.class, appDef.getPropertyGroups().get(0));
		assertEquals(SimpleParamsWAliasDuplicate.class, appDef.getPropertyGroups().get(1));
		
		//Point list
		assertEquals(3, appDef.getProperties().size());
		assertEquals(SimpleParamsWAlias.KVP_BOB, appDef.getProperties().get(0));
		assertEquals(SimpleParamsWAlias.FLAG_FALSE, appDef.getProperties().get(1));
		assertEquals(SimpleParamsWAliasDuplicate.FLAG_TRUE, appDef.getProperties().get(2));
		
		//Points for Group
		assertEquals(2, appDef.getPropertiesForGroup(SimpleParamsWAlias.class).size());
		assertEquals(SimpleParamsWAlias.KVP_BOB, appDef.getPropertiesForGroup(SimpleParamsWAlias.class).get(0));
		assertEquals(SimpleParamsWAlias.FLAG_FALSE, appDef.getPropertiesForGroup(SimpleParamsWAlias.class).get(1));
		assertEquals(1, appDef.getPropertiesForGroup(SimpleParamsWAliasDuplicate.class).size());
		assertEquals(SimpleParamsWAliasDuplicate.FLAG_TRUE, appDef.getPropertiesForGroup(SimpleParamsWAliasDuplicate.class).get(0));
		assertEquals(0, appDef.getPropertiesForGroup(SimpleParamsNoAlias.class).size());		//A random group that is not registered 
	}
	
	@Test
	public void testNonValidDefaultValueAndInvalidRegexValidationSpec() {
		
		NamingStrategy bns = new BasicNamingStrategy();
		
		RuntimeDefinition appDef = new RuntimeDefinition();
		appDef.addProperty(BadDefaultAndValidationGroup.class, BadDefaultAndValidationGroup.NAME_WITH_BAD_REGEX, 
				bns.buildNames(BadDefaultAndValidationGroup.NAME_WITH_BAD_REGEX, BadDefaultAndValidationGroup.class, "NAME_WITH_BAD_REGEX"));
		appDef.addProperty(BadDefaultAndValidationGroup.class, BadDefaultAndValidationGroup.COLOR_WITH_BAD_DEFAULT, 
				bns.buildNames(BadDefaultAndValidationGroup.COLOR_WITH_BAD_DEFAULT, BadDefaultAndValidationGroup.class, "COLOR_WITH_BAD_DEFAULT"));
		appDef.addProperty(BadDefaultAndValidationGroup.class, BadDefaultAndValidationGroup.COLOR_WITH_OK_DEFAULT, 
				bns.buildNames(BadDefaultAndValidationGroup.COLOR_WITH_OK_DEFAULT, BadDefaultAndValidationGroup.class, "COLOR_WITH_OK_DEFAULT"));
		
		assertEquals(1, appDef.getProperties().size());
		assertEquals(BadDefaultAndValidationGroup.COLOR_WITH_OK_DEFAULT, appDef.getProperties().get(0));
		assertEquals(2, appDef.getConstructionProblems().size());
		assertTrue(appDef.getConstructionProblems().get(0) instanceof ConstructionProblem.InvalidValidationConfiguration);
		assertTrue(appDef.getConstructionProblems().get(1) instanceof ConstructionProblem.InvalidDefaultValue);
		
		ConstructionProblem.InvalidValidationConfiguration invalidConfig = (ConstructionProblem.InvalidValidationConfiguration)appDef.getConstructionProblems().get(0);
		ConstructionProblem.InvalidDefaultValue invalidDefault = (ConstructionProblem.InvalidDefaultValue)appDef.getConstructionProblems().get(1);

		
		
		assertEquals(BadDefaultAndValidationGroup.NAME_WITH_BAD_REGEX, invalidConfig.getBadProperty().getProperty());
		assertEquals(BadDefaultAndValidationGroup.class, invalidConfig.getBadProperty().getGroup());
		assertEquals(false, invalidConfig.getValidator().isSpecificationValid());
		assertEquals(BadDefaultAndValidationGroup.COLOR_WITH_BAD_DEFAULT, invalidDefault.getBadProperty().getProperty());
		assertEquals(BadDefaultAndValidationGroup.class, invalidDefault.getBadProperty().getGroup());


	}
	
	/**
	 * Used for testing bad default value (don't match the validator) and bad validator config (invalid regex).
	 */
	public static interface BadDefaultAndValidationGroup extends PropertyGroup {
		StrProp NAME_WITH_BAD_REGEX = StrProp.builder().mustMatchRegex("The[broekn.*").defaultValue("The Big Chill").build();
		StrProp COLOR_WITH_BAD_DEFAULT = StrProp.builder().mustMatchRegex("[A-F,0-9]*").defaultValue("Red").build();
		StrProp COLOR_WITH_OK_DEFAULT = StrProp.builder().mustMatchRegex("[A-F,0-9]*").defaultValue("FFF000").build();

	}
}
