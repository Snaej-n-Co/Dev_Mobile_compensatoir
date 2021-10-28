package fr.efrei.reagency.bo;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.sql.Blob;
import java.util.Comparator;

//import javax.sql.rowset.serial.SerialBlob;

@Entity
public final class Property
    implements Serializable
{
    //This class is used in order to sort a Property
    public static final class PropertyComparator
            implements Comparator<Property>
    {

        @Override
        public int compare(@NotNull Property o1, @NotNull Property o2)
        {
            return o1.propertyType.compareTo(o2.propertyType);
        }

    }

    @PrimaryKey(autoGenerate = true)
    public int id;

    @NonNull
    public final String propertyType;

    @NonNull
    public final int propertyPrice;

    @NonNull
    public final int propertySurface;

    @NonNull
    public final int propertyRoom;

    @NonNull
    public final String propertyDescription;

    @NonNull
    public final String propertyAddress;

    @NonNull
    public final String propertyLatitude;

    @NonNull
    public final String propertyLongitude;

    @NonNull
    public final String propertyDateCreation;

    @NonNull
    public final String propertyDateUpdate;

    @NonNull
    public final String propertyDateSold;

    @NonNull
    public final String propertyStatus;

    @NonNull
    public final String propertyAgentName;

    @ColumnInfo(typeAffinity = ColumnInfo.BLOB)
    public final byte[] imageBlob;

    public Property(@NonNull String propertyType,
                    @NonNull int propertyPrice, @NonNull int propertySurface,
                    @NonNull int propertyRoom, @NonNull String propertyDescription,
                    @NonNull String propertyAddress, @NonNull String propertyLatitude,
                    @NonNull String propertyLongitude, @NonNull String propertyDateCreation,
                    @NonNull String propertyDateUpdate, @NonNull String propertyDateSold,
                    @NonNull String propertyStatus,
                    @NonNull String propertyAgentName, @NonNull byte[] imageBlob)
    {
        //we set the id to 0 because 0 is considerating  as not-set while inserting the item
        this.id = 0;
        this.propertyType = propertyType;
        this.propertyPrice = propertyPrice;
        this.propertySurface = propertySurface;
        this.propertyRoom = propertyRoom;
        this.propertyDescription = propertyDescription;
        this.propertyAddress = propertyAddress;
        this.propertyLatitude = propertyLatitude;
        this.propertyLongitude = propertyLongitude;
        this.propertyDateCreation = propertyDateCreation;

            this.propertyDateUpdate = propertyDateUpdate;

            this.propertyDateSold = propertyDateSold;
        this.propertyStatus = propertyStatus;
        this.propertyAgentName = propertyAgentName;
        this.imageBlob = imageBlob;
      }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof Property))
        {
            return false;
        }

        Property property = (Property) o;

        if (!propertyType.equals(property.propertyType))
        {
            return false;
        }
        if (propertyPrice != property.propertyPrice)
        {
            return false;
        }
        if (propertySurface != property.propertySurface)
        {
            return false;
        }
        if (propertyRoom != property.propertyRoom)
        {
            return false;
        }
        if (!propertyDescription.equals(property.propertyDescription))
        {
            return false;
        }
        if (!propertyAddress.equals(property.propertyAddress))
        {
            return false;
        }
        if (!propertyLatitude.equals(property.propertyLatitude))
        {
            return false;
        }
        if (!propertyLongitude.equals(property.propertyLongitude))
        {
            return false;
        }
        if (!propertyDateCreation.equals(property.propertyDateCreation))
        {
            return false;
        }
        if (!propertyDateUpdate.equals(property.propertyDateUpdate))
        {
            return false;
        }
        if (!propertyDateSold.equals(property.propertyDateSold))
        {
            return false;
        }
        if (!propertyStatus.equals(property.propertyStatus))
        {
            return false;
        }
        return propertyAgentName.equals(property.propertyAgentName);
    }

    @Override
    public int hashCode()
    {
        int result = propertyType.hashCode();
        result = 31 * result + String.valueOf(propertySurface).hashCode();
        result = 31 * result + String.valueOf(propertySurface).hashCode();
        result = 31 * result + String.valueOf(propertyRoom).hashCode();
        result = 31 * result + propertyDescription.hashCode();
        result = 31 * result + propertyAddress.hashCode();
        result = 31 * result + propertyLatitude.hashCode();
        result = 31 * result + propertyLongitude.hashCode();
        result = 31 * result + propertyStatus.hashCode();
        result = 31 * result + propertyAgentName.hashCode();
        return result;
    }
}
