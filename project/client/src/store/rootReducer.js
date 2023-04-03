import {combineReducers} from "redux";
import checkBoxReducer from "./reducers/other/checkBoxReducer";
import themeReducer from "./reducers/main/themeReducer";
import indicatorReducer from "./reducers/start/indicatorReducer";
import newsReducer from "./reducers/newsReducer";
import contactReducer from "./reducers/contactReducer";
import statusReducer from "./reducers/statusReducer";
import dnevnikReducer from "./reducers/dnevnikReducer";
import zvonkiReducer from "./reducers/analytics/zvonkiReducer";
import periodsReducer from "./reducers/analytics/periodsReducer";
import scheduleReducer from "./reducers/analytics/scheduleReducer";
import journalReducer from "./reducers/analytics/journalReducer";
import marksReducer from "./reducers/analytics/marksReducer";
import teachersReducer from "./reducers/people/teachersReducer";
import hteachersReducer from "./reducers/people/hteachersReducer";
import classmatesReducer from "./reducers/people/classmatesReducer";
import parentsReducer from "./reducers/people/parentsReducer";
import adminsReducer from "./reducers/people/adminsReducer";
import profileReducer from "./reducers/main/profileReducer";
import pjournalReducer from "./reducers/pjournalReducer";
import paneReducer from "./reducers/other/paneReducer";
import eventsReducer from "./reducers/other/eventsReducer";
import groupReducer from "./reducers/people/groupReducer";
import requestReducer from "./reducers/requestReducer";
import dialogReducer from "./reducers/other/dialogReducer";

export default combineReducers({
    checkbox: checkBoxReducer,
    themes: themeReducer,
    indicators: indicatorReducer,
    news: newsReducer,
    contacts: contactReducer,
    states: statusReducer,
    dnevnik: dnevnikReducer,
    zvonki: zvonkiReducer,
    periods: periodsReducer,
    schedules: scheduleReducer,
    journals: journalReducer,
    marks: marksReducer,
    teachers: teachersReducer,
    hteachers: hteachersReducer,
    classmates: classmatesReducer,
    parents: parentsReducer,
    admins: adminsReducer,
    profiles: profileReducer,
    pjournal: pjournalReducer,
    pane: paneReducer,
    events: eventsReducer,
    groups: groupReducer,
    requests: requestReducer,
    dialog: dialogReducer
});