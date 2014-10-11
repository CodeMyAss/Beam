package me.aventium.projectbeam.documents;

import com.mongodb.DBObject;
import me.aventium.projectbeam.DBO;

import java.util.Date;

public class DBFriendship extends Document {

    public static final String FRIENDED_FIELD = "friended";
    public static final String FRIENDER_FIELD = "friender";
    public static final String SENT_DATE_FIELD = "sent_date";
    public static final String RESULT_FIELD = "result";
    public static final String RESULT_DATE_FIELD = "result_date";

    public DBFriendship(DBObject dbo) {
        super(dbo);
    }

    public String getFriend(String otherFriend) {
        String friend = null;
        if(otherFriend.equals(this.getFriender())) {
            friend = this.getFriended();
        } else if(otherFriend.equals(this.getFriended())) {
            friend = this.getFriender();
        }

        if(otherFriend.equals(friend)) {
            return null;
        } else {
            return friend;
        }
    }

    public String getFriender() {
        return DBO.getString(this.dbo, FRIENDER_FIELD);
    }

    public String getFriended() {
        return DBO.getString(this.dbo, FRIENDED_FIELD);
    }

    public Date getSendDate() {
        return (Date) this.dbo.get(SENT_DATE_FIELD);
    }

    public Result getResult() {
        return Result.fromDBValue((Boolean) this.dbo.get(RESULT_FIELD));
    }

    public Date getResultDate() {
        return (Date) this.dbo.get(RESULT_DATE_FIELD);
    }

    public static enum Result {
        ACCEPTED,
        DENIED,
        PENDING;

        public Boolean toDBValue() {
            switch(this) {
                case ACCEPTED: return true;
                case DENIED: return false;
                case PENDING:
                default: return null;
            }
        }

        public static Result fromDBValue(Boolean val) {
            if(val == null) {
                return PENDING;
            } else if(val == true) {
                return ACCEPTED;
            } else {
                return DENIED;
            }
        }
    }

}
