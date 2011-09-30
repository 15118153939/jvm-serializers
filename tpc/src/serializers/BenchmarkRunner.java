package serializers;

import java.io.*;
import java.net.URLEncoder;
import java.util.*;
import java.util.regex.Pattern;
import java.util.zip.DeflaterOutputStream;

import serializers.BenchmarkBase.Params;
import serializers.cks.CksBinary;
import serializers.cks.CksText;
import serializers.jackson.*;
import serializers.json.JsonGsonDatabind;
import serializers.json.JsonArgoTree;
import serializers.json.FastJSONDatabind;
import serializers.json.FlexjsonDatabind;
import serializers.json.JsonGsonManual;
import serializers.json.JsonGsonTree;
import serializers.json.JsonDotOrgManualTree;
import serializers.json.JsonLibJsonDatabind;
import serializers.json.JsonPathDeserializerOnly;
import serializers.json.JsonSimpleManualTree;
import serializers.json.JsonSimpleWithContentHandler;
import serializers.json.JsonSmartManualTree;
import serializers.json.JsonTwoLattes;
import serializers.json.JsonijJpath;
import serializers.json.JsonijManualTree;
import serializers.json.JsonSvensonDatabind;
import serializers.protobuf.ActiveMQProtobuf;
import serializers.protobuf.Protobuf;
import serializers.protostuff.Protostuff;
import serializers.protostuff.ProtostuffJson;
import serializers.protostuff.ProtostuffSmile;
import serializers.xml.XmlJavolution;
import serializers.xml.XmlStax;
import serializers.xml.XmlXStream;

public class BenchmarkRunner extends BenchmarkBase
{
    public static void main(String[] args)
    {
        new BenchmarkRunner().runBenchmark(args);
    }

    @Override
    protected void addTests(TestGroups groups)
    {
        // Binary Formats; language-specific ones
        JavaBuiltIn.register(groups);
        JavaManual.register(groups);
        Scala.register(groups);
        // hessian, kryo and wobly are Java object serializations
        Hessian.register(groups);
        Kryo.register(groups);
        Wobly.register(groups);
        
        // Binary formats, generic: protobuf, thrift, avro, CKS, msgpack
        Protobuf.register(groups);
        ActiveMQProtobuf.register(groups);
        Protostuff.register(groups);
        Thrift.register(groups);
        AvroSpecific.register(groups);
        AvroGeneric.register(groups);
        CksBinary.register(groups);
        MsgPack.register(groups);

        // JSON
        JacksonJsonManual.register(groups);
        JacksonJsonTree.register(groups);
        JacksonJsonTreeWithStrings.register(groups);
        JacksonJsonDatabind.register(groups);
        JacksonJsonDatabindWithStrings.register(groups);
        JsonTwoLattes.register(groups);
        ProtostuffJson.register(groups);
// too slow, why bother:
//        ProtobufJson.register(groups);
        JsonGsonManual.register(groups);
        JsonGsonTree.register(groups);
        JsonGsonDatabind.register(groups);
        JsonSvensonDatabind.register(groups);
        FlexjsonDatabind.register(groups);
        JsonLibJsonDatabind.register(groups);
        FastJSONDatabind.register(groups);
        JsonSimpleWithContentHandler.register(groups);
        JsonSimpleManualTree.register(groups);
        JsonSmartManualTree.register(groups);
        JsonDotOrgManualTree.register(groups);
        JsonijJpath.register(groups);
        JsonijManualTree.register(groups);
        JsonArgoTree.register(groups);
        JsonPathDeserializerOnly.register(groups);

        // Then JSON-like
        // CKS text is textual JSON-like format
        CksText.register(groups);
        // then binary variants
        // BSON is binary JSON-like format
        JacksonBsonManual.register(groups);
        JacksonBsonDatabind.register(groups);
        MongoDB.register(groups);
        JacksonSmileManual.register(groups);
        JacksonSmileDatabind.register(groups);
        ProtostuffSmile.register(groups);

        // XML-based formats.
        XmlStax.register(groups);
        XmlXStream.register(groups);
        JacksonXmlDatabind.register(groups);
        XmlJavolution.register(groups);
    }

    @Override
    protected <J> void checkCorrectness(PrintWriter errors, Transformer<J,Object> transformer,
            Serializer<Object> serializer, J value)
        throws Exception
    {
        Object specialInput;
        String name = serializer.getName();
        try {
            specialInput = transformer.forward(value);
        }
        catch (Exception ex) {
            System.out.println("ERROR: \"" + name + "\" crashed during forward transformation.");
            errors.println(ERROR_DIVIDER);
            errors.println("\"" + name + "\" crashed during forward transformation.");
            ex.printStackTrace(errors);
            return;
        }

        byte[] array;
        try {
            array = serializer.serialize(specialInput);
        }
        catch (Exception ex) {
            System.out.println("ERROR: \"" + name + "\" crashed during serialization.");
            errors.println(ERROR_DIVIDER);
            errors.println("\"" + name + "\" crashed during serialization.");
            ex.printStackTrace(errors);
            return;
        }

        Object specialOutput;

        try {
            specialOutput = serializer.deserialize(array);
        }
        catch (Exception ex) {              
            System.out.println("ERROR: \"" + name + "\" crashed during deserialization.");
            errors.println(ERROR_DIVIDER);
            errors.println("\"" + name + "\" crashed during deserialization.");
            ex.printStackTrace(errors);
            return;
        }

        J output;
        try {
            output = transformer.reverse(specialOutput);
        }
        catch (Exception ex) {
            System.out.println("ERROR: \"" + name + "\" crashed during reverse transformation.");
            errors.println(ERROR_DIVIDER);
            errors.println("\"" + name + "\" crashed during reverse transformation.");
            ex.printStackTrace(errors);
            return;
        }
        if (!value.equals(output)) {
            System.out.println("ERROR: \"" + name + "\" failed round-trip check.");
            errors.println(ERROR_DIVIDER);
            errors.println("\"" + name + "\" failed round-trip check.");
            errors.println("ORIGINAL:  " + value);
            errors.println("ROUNDTRIP: " + output);

            System.err.println("ORIGINAL:  " + value);
            System.err.println("ROUNDTRIP: " + output);
                    
        }
    }
}
