package se.unlogic.eagledns.zoneproviders.db.beans;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xbill.DNS.DClass;
import org.xbill.DNS.Name;
import org.xbill.DNS.RRset;
import org.xbill.DNS.Record;
import org.xbill.DNS.SOARecord;
import org.xbill.DNS.Type;
import org.xbill.DNS.Zone;
import se.unlogic.standardutils.dao.annotations.DAOManaged;
import se.unlogic.standardutils.dao.annotations.Key;
import se.unlogic.standardutils.dao.annotations.OneToMany;
import se.unlogic.standardutils.dao.annotations.Table;
import se.unlogic.standardutils.xml.Elementable;
import se.unlogic.standardutils.xml.XMLElement;
import se.unlogic.standardutils.xml.XMLGenerator;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@XMLElement
@Table(name="zones")
public class DBZone implements Elementable{

   @DAOManaged(autoGenerated=true)
   @Key
   @XMLElement
   private Integer zoneID;

   @DAOManaged
   @XMLElement
   private String name;

   @DAOManaged
   @XMLElement
   private String dclass;

   @DAOManaged
   @XMLElement
   private Long ttl;

   @DAOManaged
   @XMLElement
   private String primaryDNS;

   @DAOManaged
   @XMLElement
   private String adminEmail;

   @DAOManaged
   @XMLElement
   private Long serial;

   @DAOManaged
   @XMLElement
   private Long refresh;

   @DAOManaged
   @XMLElement
   private Long retry;

   @DAOManaged
   @XMLElement
   private Long expire;

   @DAOManaged
   @XMLElement
   private Long minimum;

   @DAOManaged
   @OneToMany
   @XMLElement
   private List<DBRecord> records;

   @DAOManaged
   @XMLElement
   private boolean secondary;

   @DAOManaged
   @XMLElement
   private Timestamp downloaded;

   public DBZone() {

      super();
   }

   public DBZone(final Zone zone, final boolean secondary) {

      this.parse(zone, secondary);
   }

   public void parse(Zone zone, boolean secondary) {

      if(zone == null){

         this.ttl = null;
         this.adminEmail = null;
         this.serial = null;
         this.refresh = null;
         this.retry = null;
         this.expire = null;
         this.minimum = null;
         this.records = null;
         this.downloaded = null;

      }else{

         SOARecord soaRecord = zone.getSOA();

         this.name = soaRecord.getName().toString();
         this.dclass = DClass.string(soaRecord.getDClass());
         this.ttl = soaRecord.getTTL();
         this.primaryDNS = soaRecord.getHost().toString();
         this.adminEmail = soaRecord.getAdmin().toString();
         this.serial = soaRecord.getSerial();
         this.refresh = soaRecord.getRefresh();
         this.retry = soaRecord.getRetry();
         this.expire = soaRecord.getExpire();
         this.minimum = soaRecord.getMinimum();
         this.secondary = secondary;

         if(secondary){
            this.downloaded = new java.sql.Timestamp(System.currentTimeMillis());
         }

         this.records = new ArrayList<DBRecord>();

         Iterator<?> iterator = zone.iterator();

         while(iterator.hasNext()){

            RRset rRset = (RRset) iterator.next();

            Iterator<?> rrSetIterator = rRset.rrs();

            while(rrSetIterator.hasNext()){

               Record record = (Record) rrSetIterator.next();

               if(record.getType() == Type.SOA){
                  continue;
               }

               this.records.add(new DBRecord(record, zone.getSOA().getName(), this.ttl));
            }
         }
      }
   }

   public String getName() {

      return name;
   }


   public void setName(String name) {

      this.name = name;
   }


   public String getDclass() {

      return dclass;
   }


   public void setDclass(String dclass) {

      this.dclass = dclass;
   }


   public Long getTtl() {

      return ttl;
   }


   public void setTtl(Long ttl) {

      this.ttl = ttl;
   }


   public String getPrimaryDNS() {

      return primaryDNS;
   }


   public void setPrimaryDNS(String primaryDns) {

      this.primaryDNS = primaryDns;
   }


   public String getAdminEmail() {

      return adminEmail;
   }


   public void setAdminEmail(String adminEmail) {

      this.adminEmail = adminEmail;
   }


   public Long getSerial() {

      return serial;
   }


   public void setSerial(Long serial) {

      this.serial = serial;
   }


   public Long getRefresh() {

      return refresh;
   }


   public void setRefresh(Long refresh) {

      this.refresh = refresh;
   }


   public Long getRetry() {

      return retry;
   }


   public void setRetry(Long retry) {

      this.retry = retry;
   }


   public Long getExpire() {

      return expire;
   }


   public void setExpire(Long expire) {

      this.expire = expire;
   }


   public Long getMinimum() {

      return minimum;
   }


   public void setMinimum(Long minimum) {

      this.minimum = minimum;
   }

   public List<DBRecord> getRecords() {

      return records;
   }


   public void setRecords(List<DBRecord> records) {

      this.records = records;
   }


   public Integer getZoneID() {

      return zoneID;
   }


   public void setZoneID(Integer zoneID) {

      this.zoneID = zoneID;
   }

   public Element toXML(Document doc) {

      return XMLGenerator.toXML(this, doc);
   }

   public Zone toZone() throws IOException{

      Name zoneName = Name.fromString(name);
      Name primaryNS = Name.fromString(this.primaryDNS);

      SOARecord soaRecord = new SOARecord(zoneName, DClass.value(dclass), ttl, primaryNS, Name.fromString(this.adminEmail), serial, refresh, retry, expire, minimum);

      //Record primaryNSRecord = Record.newRecord(primaryNS, Type.NS, DClass.value(dclass), ttl);

      int recordCount;

      if(this.records != null){

         recordCount = 1 + this.records.size();

      }else{

         recordCount = 1;
      }

      Record[] recordArray = new Record[recordCount];

      recordArray[0] = soaRecord;
      //recordArray[1] = primaryNSRecord;

      if(records != null){

         int pos = 1;

         for(DBRecord record : this.records){

            recordArray[pos] = record.getRecord(ttl,zoneName);

            pos++;
         }
      }

      return new Zone(zoneName,recordArray);
   }


   public boolean isSecondary() {

      return secondary;
   }


   public void setSecondary(boolean secondary) {

      this.secondary = secondary;
   }

   @Override
   public String toString() {

      return name + " (ID: " + zoneID + ")";
   }


   public Timestamp getDownloaded() {

      return downloaded;
   }


   public void setDownloaded(Timestamp zoneDownloaded) {

      this.downloaded = zoneDownloaded;
   }
}
