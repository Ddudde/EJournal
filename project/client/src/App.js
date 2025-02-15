import {Route, Routes, useNavigate} from "react-router-dom";
import Main from "./components/main/Main";
import Start from "./components/start/Start.jsx";
import NewsMain from "./components/news/NewsMain";
import NewsYo from "./components/news/NewsYo";
import NewsPor from "./components/news/NewsPor";
import ContactMain from "./components/contacts/ContactMain";
import ContactYo from "./components/contacts/ContactYo";
import ContactPor from "./components/contacts/ContactPor";
import ErrFound from "./components/other/error/ErrFound";
import {states} from "./store/selector";
import {useSelector} from "react-redux";
import Dnevnik from "./components/dnevnik/Dnevnik";
import AnalyticsMain from "./components/analytics/AnalyticsMain";
import Zvonki from "./components/analytics/zvonki/Zvonki";
import Periods from "./components/analytics/periods/Periods";
import Schedule from "./components/analytics/schedule/Schedule";
import AnalyticsJournal from "./components/analytics/journal/AnalyticsJournal";
import Marks from "./components/analytics/marks/Marks";
import PeopleMain from "./components/people/PeopleMain";
import Teachers from "./components/people/Teachers";
import HTeachers from "./components/people/HTeachers";
import Classmates from "./components/people/Classmates";
import Parents from "./components/people/parents/Parents";
import Admins from "./components/people/Admins";
import React, {useEffect, useRef} from "react";
import Profile from "./components/main/profile/Profile";
import Settings from "./components/main/settings/Settings";
import Tutor from "./components/tutor/Tutor";
import Journal from "./components/prepjur/TeacherJournal.jsx";
import Request from "./components/request/Request";
import Redirect from "./components/main/Redirect";
import Test from "./components/test/Test";

function App() {
    const cState = useSelector(states);
    const navigate = useNavigate();
    const isFirstUpdate = useRef(true);
    let indexComp, path;
    if(!cState.auth) {
        indexComp = <Start/>;
    } else {
        if(cState.role < 2) indexComp = <Dnevnik/>;
        if(cState.role == 2) indexComp = <Schedule/>;
        if(cState.role == 3) indexComp = <AnalyticsMain comp={<Zvonki/>}/>;
        if(cState.role == 4) indexComp = <Request/>;
    }
    useEffect(() => {
        console.log("I was triggered during componentDidMount App");
        path = localStorage.getItem('path');
        if(path) {
            localStorage.removeItem('path');
            console.log("path...");
            console.log(path);
            navigate(path);
        }
        return function() {
            console.log("I was triggered during componentWillUnmount App")
        }
    }, []);
    useEffect(() => {
        if (isFirstUpdate.current) {
            isFirstUpdate.current = false;
            return;
        }
        console.log('componentDidUpdate App');
    });
    return <Routes>
        <Route path="/" element={<Redirect/>}/>
        <Route path="EJournal" element={<Main/>}>
            <Route index element={indexComp}/>
            <Route path="news" element={<NewsMain/>}>
                <Route index element={<NewsPor/>} />
                <Route path="por" element={<NewsPor/>} />
                {(cState.auth && cState.role != 4) && <Route path="yo" element={<NewsYo/>} />}
            </Route>
            <Route path="contacts" element={<ContactMain/>}>
                <Route index element={<ContactPor/>} />
                <Route path="por" element={<ContactPor/>} />
                {(cState.auth && cState.role != 4) && <Route path="yo" element={<ContactYo/>} />}
            </Route>
            {(cState.auth && (cState.role < 2 || cState.role == 3)) && <Route path={cState.role == 3 ? "" : "analytics"} element={<AnalyticsMain/>}>
                <Route index element={<Zvonki/>} />
                <Route path="zvonki" element={<Zvonki/>} />
                <Route path="periods" element={<Periods/>} />
                <Route path="schedule" element={<Schedule/>} />
                {(cState.auth && cState.role < 2) && <Route path="journal" element={<AnalyticsJournal/>} />}
                {(cState.auth && cState.role < 2) && <Route path="marks" element={<Marks/>} />}
            </Route>}
            <Route path="people" element={<PeopleMain/>}>
                <Route index element={<Admins/>} />
                {(cState.auth && (cState.role < 2 || cState.role == 3)) && <Route path="teachers" element={<Teachers/>} />}
                {cState.auth && <Route path="hteachers" element={<HTeachers/>} />}
                {(cState.auth && (cState.role == 0 || cState.role == 3)) && <Route path="class" element={<Classmates/>} />}
                {(cState.auth && (cState.role == 0 || cState.role == 3)) && <Route path="parents" element={<Parents/>} />}
                <Route path="admins" element={<Admins/>} />
            </Route>
            {(!cState.auth || cState.role < 4) && <Route path="tutor/:typ" element={<Tutor/>} />}
            {cState.auth && <Route path="profiles" element={<Profile/>} />}
            {(cState.auth && cState.role == 2) && <Route path="journal" element={<Journal/>} />}
            <Route path="profiles/:log" element={<Profile/>} />
            {cState.auth && <Route path="settings" element={<Settings/>} />}
            {(cState.auth && cState.role == 4) && <Route path="test" element={<Test/>} />}
            <Route path="invite/:code" element={<Start mod="inv"/>} />
            <Route path="reauth/:code" element={<Start mod="rea"/>} />
            <Route path="*" element={<ErrFound/>} />
        </Route>
    </Routes>;
}

export default App;
