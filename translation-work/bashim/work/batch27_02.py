#!/usr/bin/env python3
"""Translate bashim chunk_27 batch 2 (input lines 101-200). ru is read verbatim
from the input file; only en translations are embedded below."""
import json

EN = {
409175: """xxx: what pisses me off is they treat me like the many-armed Shiva, but pay me like a one-legged macaque.""",
409176: """This morning at the meeting:

boss: Little sayings like "Work isn't a wolf - it won't run into the forest" - that's not about our company! And "War is war, but lunch is on schedule" - not about our company!"

a manager's sad voice: Work till you drop, and when you drop - hand over your cases. THAT's about our company.""",
409177: """xxx: turns out there's a list of groceries to buy for New Year's hanging on the wall. left over from last year.
yyy: convenient, no need to think about what to buy""",
409178: """<UncleFedor> while walking to the store, I pushed four cars out
<UncleFedor> not going anymore :)""",
409179: """ххх: Exams are coming soon. The students' politeness has become simply unbearable...""",
409180: """xxx: our local news showed a story. two dudes got drunk and decided to take revenge on the neighbor who parks his car in one of the dudes' spot. went to piss in his gas tank and one of them froze to it (-30 something degrees it was), they called an ambulance. opened a hooliganism case.

yyy: should've been a "member-injury" case )""",
409181: """ххх: Logged into VK for the first time in ages to see what's up. Saw an ad: "Great Empire. Lead your army to victory". Thought: "Why not?". Launched Rome: Total War and spent the whole night conquering Gaul..""",
409182: """xxx: so, did you meet your guy from the army? )
yyy: yeah-yeah
xxx: has he changed? )
yyy: just a tiny bit
xxx: grown horns?""",
409183: """From Habr:

socket378: It's long surprised and alarmed me how easily lawmakers meddle in IT while being absolutely incompetent in it. Seems like anyone who's mastered the "Start" button already thinks they know "how to fix the internet". Why don't they meddle in nuclear physics, for example? What has IT done to deserve this?

Why not pass a law, say, "on energy efficiency", obliging electricity producers to use only perpetual motion machines? Where they'd get those machines is a secondary question, but the initiative is a good one! Free energy would boost GDP a trillionfold, everyone would be happy.

And it'd also be great to pass a law "on countering hunger", under which food producers would be obliged to feed everyone who wants it, and also keep an eye out themselves so nobody goes hungry. How producers would wriggle out of that - I don't know, but what a life we'd have!""",
409184: """xxx: 19:08:08 31/12/2010: Never, you hear me, never update servers on December 31st!""",
409185: """xxx: Can't wait for social networks to get into cars. You overtake a traffic jam on the shoulder and bam: "14 people think you're a dickhead\".""",
409186: """xxx: Bought my nephew a Spider-Man mask, you know, all dark with the webbing. And grandma and grandpa gave him a Bunny costume.
xxx: Now a Bunny with Spider-Man's mug is racing around the house.""",
409187: """ХХХ: Don't. No need to hide and waste your youth and beauty. There's always a worthy person out there :)
YYY: a person worthy of wasting my youth and beauty? )""",
409188: """From the forum:
Anuta_: Please tell me, why is it that every time me and my boyfriend lie in bed before sleep, he says to me "Good night, Dart Vader\"?""",
409189: """Настя: OK. Do you have an antivirus? Is it running?
Ира: yeah I do. it's running.
Настя: Well? Which one?
Ира: utorrent""",
409190: """zipman: as our safety instructor says, "nothing pleases the eye like the second eye\"""",
409191: """xxx: my cat sneezed, and I, without thinking: "Bless you" ))
yyy: the main thing is he didn't answer just as thoughtlessly "thanks\")""",
409192: """Once the bitten: so, what stage is the cleaning at?
Надюшкин: Planting roses
Once the bitten: and have you sorted the groats yet?""",
409193: """Bravin: and I see all the cases, damn it, are expensive.
Bravin: over three hundred rubles apiece.
Bravin: cash for a tiny piece of rag around a cell phone...
ххх: damn right, damn it
ххх: you, damn it, are a Gentoo user!
ххх: you, damn it, didn't just build your own OS
ххх: you, damn it, built your own cell phone too, damn it
ххх: and you're bitching, damn it, about a rag for your cell phone!
ххх: YEAH, DAMN IT, GO SEW ONE YOURSELF!
Bravin: now that's a thought!""",
409194: """xxx:  yesterday I failed the daily quest
xxx:  at work they tried to foist a toy hare on me. I refused, too lazy to lug it
xxx:  an hour later - a text from mom: "Honey, I need a toy hare. Urgently. We're visiting your niece\".""",
409195: """- we've got several pregnant women in the department already. everyone confessed, but I haven't yet. every time I look at the boss, I picture it, and I don't know how to say it.
- well, show it in pantomime. from the very beginning.""",
409196: """The other day I was roller-skating with my eleven-year-old daughter and her friends. Eating ice cream, one of the girls, looking up with her little eyes, uttered:

- I want to pierce my lower lip and eyebrow, like Nastya and Polina's.

- Why? - I was surprised.

- So I can be different from everyone else!""",
409197: """One AM, trying to smoke my sister out of the bathroom:
- Listen, are you ever coming out of there?
A voice from behind the door:
- Explain to me why I'd need that? Outside it's a cruel world!..""",
409198: """<hatarisu> Do you need porn on floppies? I've got some in better quality, and it's a shame to throw away...""",
409199: """xxx: You've probably all seen the ad about the footballer who catches a penalty kick and bursts out of the TV into a room with fans, where everyone's happy to see him.
xxx: I wonder what would've happened if he'd burst into a room where the other team's fans were sitting?""",
409200: """xxx: what was the scariest thing in your life? :)
yyy: well, probably when I jumped with a parachute I'd packed myself""",
409201: """Today I was walking down the street and overheard a dialogue:
A kid (in a classic tantrum):
- Daaad! Buy me a caar!!
The father:
- I will. A washing machine.""",
409202: """From Habr, a comment on the new Russian smartphone:
Don't worry, the GLONASS chip will be reliably installed there with double-sided tape.""",
409203: """Asking my sister, she's a lawyer
me: have you ever thought about what comes after the Last Judgment?
sister: never thought about it, I know what comes - the Terrible Arbitration Court""",
409204: """ХХХ: They sent me pelmeni from the village. Walking home, thinking I'll cook them. Put water on, bring to a boil. Take the pelmeni and start launching them in. And I notice the dough starting to slide off them... Turned out to be cookies in white chocolate.""",
409205: """Mrs_Mud: We had this guy. Every New Year he'd go to Chernobyl and saw down Christmas trees there. Then sold them for 200 hryvnias each, saying each one glows at night and can grow 5 cm overnight!
Mrs_Mud: And people fell for it, bought them...""",
409206: """xxx: Can't you take a look? Something popped out on me.
xxx: I'm a sysadmin, not a dermatologist!""",
409207: """юзверь: Good afternoon, our xerox won't print.
me: It's not supposed to print, it makes copies.
юзверь: Oh, thanks!""",
409208: """Марихуана: Remember Tanya? She's preganant. And such a quiet girl.
Vinnetu: quiet girls have kids in them""",
409209: """xxx: Found at grandma's a bound volume of Murzilka magazine from 1979.
xxx: Looked at the little readers' letters there, then searched for them on Odnoklassniki.
xxx: They've grown up.""",
409210: """kotw: I get it
kotw: Russian women had to learn to "stop a galloping horse" so the prince wouldn't gallop past""",
409211: """xxx: At work we've got this one dude, every day he eats one kiwi.
xxx: First he shaves the kiwi with office scissors, then eats it with the skin.""",
409212: """xxx: Something's wrong with my laptop, it boots, a picture appears, then a blue screen and it goes dark.
yyy: Something with the hardware.
xxx: There's no hardware in my laptop, it's plasstic!""",
409213: """xxx: And my ex-boyfriend, when asked to give his cat to his mom (I'm allergic), said the cat's been with him 10 years, and I've only been around a month and a half, and we broke up. Is that even normal?""",
409214: """xxx: I'm hungry, what can I cook?
yyy: fried eggs
yyy: first, find an egg
yyy: preferably a chicken one
xxx: nothing simpler? )
yyy: sure, walk up to the windowsill, tear a leaf off the plant and eat it, you can salt it
yyy: that work for ya?
xxx: =(
yyy: why so glum? you got a cactus standing there or what?""",
409215: """From L2:
Экстази: hi, let's get acquainted?
Настенька: first give me 100k :)
...
Экстази: well:) my name's Tyoma :)
Настенька: mine too
Настенька: oops""",
409216: """X: Going on vacation?
Y: Yeah
X: Where to, Sheremetyevo or Domodedovo?""",
409217: """Saturday. Lying on the couch "sick", torturing my phone. A floor above, a neighbor with a hammer drill moved in and for about a month now has been methodically drilling something. Having nothing better to do, I turn on the phone's "blue tooth", which immediately finds someone's handset. No harm in trying: in the settings I rename myself to "turn it off, you bastard, the drill: I'm sick of it" and send a connection request. Five seconds later the drill goes silent and wild laughter comes from upstairs. Haven't heard the neighbor for two days now.""",
409218: """Pass-fail in psychology and pedagogy.
Prof: "Fill in your record books. Whoever writes "Psycho and Pedo" - no pass for you!\"""",
409219: """ххх: I feel like our conversation has hit a dead end
ууу: Weell.. let's continue it from the point where it wasn't in a dead end yet =D
ххх: let's
ххх: hi""",
409220: """IllBeBack:
Came to work and got tired, and there's still 30 minutes before it starts :(""",
409221: """From the Sevastopol forum:
begemott: yeah, I remember since childhood - "Moscow time 15:00 ... in Petropavlovsk-Kamchatsky - midnight" and dad's first shot went down...""",
409222: """mudakisa: A letter comes from a friend with photos. The email subject is "animals". The photos are big and load slowly. only when the process finishes does it become clear that they're photos from the corporate party.""",
409223: """SHmEl''': the kids asked Anya (my girlfriend's a teacher, 23) in class what a cassette is.""",
409224: """xxx: 90% of failed dates start with the phrase: "Let's meet at that exit, the one that ...\"""",
409225: """zombiracer: we have a tradition
zombiracer: every year on December 31st
zombiracer: the non-drinkers watch the same movies""",
409226: """Doddy: A teaspoon left overnight in the sink attracts a ton of unwashed dishes to itself.""",
409227: """xxx: every morning my neighbor drills the wall with a hammer drill, and in the evenings now he plays a drum kit. Seriously. I think I live next to a superman.""",
409228: """xxx: I'm noticing more and more that you can drive a person into a light stupor by asking them to "say any word\"""",
409229: """admin: they say not inviting the admin to the corporate party is a bad omen.
boss: why?
admin: there'll be no internet all year.""",
409230: """xxx: Listen, don't you remember what's that movie where Seagal takes everyone out single-handed??""",
409231: """Lord Antony: Today I saw a real picture of the day - a person hauling 100 bottles of vodka and a CAKE out of the supermarket.""",
409232: """xxx: Some girls on social networks like to add the letters tm to their first/last name in superscript.
xxx: Does that mean that, being a registered trademark, they're selling themselves?""",
409233: """<Транк> they didn't deliver my ordered Christmas tree. said because of the traffic jams they'll bring it after the holidays...""",
409234: """xxx: Mom asked me to clean up at home.
xxx: I hinted that I was planning to go visit someone today and anyway, it's New Year, a holiday, after all.
xxx: Now I'm cleaning up wearing pink bunny ears.""",
409235: """talking to a patient. she says she's heard a voice in her head for years - first a child's, then it went to school, finished it, got into college, defended its thesis...
"it was so interesting with him, I talked to him, raised him, and after your pills he disappeared. now I'm so lonely, give me back my voice!"...
my first thought: "he got drafted into the army\".""",
409236: """xxx: Katya, you see, he's not just a moron. I'm sure he's got a medal lying around at home somewhere for first place in a contest among the mentally retarded. There are so many guys around, why, why did I have to like him of all people?
yyy: You just always pick winners)""",
409237: """xxx: before New Year's I thought this dress doesn't wrinkle""",
409238: """xxx: Watching a movie about the oracle.
xxx: The kid comes up - Did the Delphic Oracle write in Delphi or in Oracle?""",
409239: """At the intersection in the city center, near the mall, there's a patch warmed by a heating main where pigeons warm themselves. God knows how many heads. A cloud.
Across the road - a parking lot of cars that drove up to the mall - Nissans, Lexuses, Mercs, beamers and other elite of the auto industry.
A lady comes out of the store and, before getting into her assmobile, decided to show off her intellect and charity - unwraps a pack of sunflower seeds and, striking a sower's pose, hurls the whole pack onto the parking lot.
The entire hungry winged brotherhood takes off from the patch and, to the curses of the humans in its path, circles over the parking lot, coming in to land, while generously showering the gawkers and cars with fertilizer. The result - some 20 fully fertilized assmobiles, and as many people who were standing on the crosswalk. How the intellectual lady got out of there - I didn't see, I figure very fast.""",
409240: """xxx: an animal show:
xxx: "A young cormorant is pestering iguanas peacefully basking in the sun. Just like that, out of boredom"
xxx: what a loser.""",
409241: """х: Did they show "Irony of Fate" at your place again?
у: Yep. And at yours?
х: At ours they were pushing "2012". Well, like, it's high time to start introducing this New Year tradition of watching 2012 instead of Irony. No guarantee the tradition will last long, but still...""",
409242: """Oredium: turns out the iPhone catches underwater
Merk: fish?""",
409243: """<Den64> So which is it, the year of the cat or the rabbit?
<Nophi> Why not combine them?
<Nophi> The year of the mole!
<Helena> The year of the catbit!!""",
409244: """Rowen: In honor of the New Year the devs are giving all players 2500 gold!
Rowen: they've totally lost it
Rowen: I've still got five exams""",
409245: """Шушарик: fell asleep at night. dreamed my husband was calling president Medvedev and saying they weren't paying his salary, and he was crying into the phone saying it's Putin...""",
409246: """Me: Let's pop into XXX, I saw such a cool sushi set there!
Him: Nah, don't really want to...
Me: Come on, pleease? I just want to ask your advice and that's it
Him: Screw that, after the last "ask your advice" I didn't even have enough for the bus...""",
409247: """Kot Luis: Now I see a popup with a service message, like "user Mom is typing you a message."
Kot Luis: The next popup was your message: "FUCKING FUCKED-UP FUCKING INTERNET\".""",
409248: """On the 3D graphics forum:
ХХХ: 01/01/2011, 04:06
I'm a zero in animation and kinematics, but I want to learn. The task is to make something like Dr. Otto Octavius's tentacle from Spider Man 2.
the main thing is to make it so each joint has free movement X/Y, say 20 degrees, and a bit around its own axis, say 5 degrees. And when it reaches 20 deg. the next joint starts moving.
the picture shows a simplified diagram, but the main thing for me is to understand what to make it with. Maybe bones or something else....
УУУ: 01/01/2011, 04:21
duuude, is this really what's on your mind right now?""",
409249: """For the holidays I went home for 2 weeks and shared my Wi-Fi at the rented apartment, thinking let people use it, I don't mind. I even named the network "Happy Upcoming to All". Imagine my surprise when, coming back home, I found I'd been banned on 2 trackers, 4 forums, and in my mailbox lay a note reading "You said I wouldn't find you\".""",
409250: """Ferroman> Playing the piano is also very simple - you just need to hit the right keys at the right moment.""",
409251: """xxx: And today I got some fleeting male attention :)
xxx: riding the bus, and this cute boy gets in
xxx: and sits down next to me
xxx: while sitting down, he put his hand on my knee
xxx: and then he rode along, looking at me and smiling :)
yyy: soo?
xxx: what do you mean soo
xxx: he's about 3 years old and he was sitting on his mom's lap....:(""",
409252: """xxx: someone write a virus for porn sites
xxx: so that when you watch a video the webcam turns on )))""",
409253: """arsenyk: Sorting through my father's books. Found the book "Higher Transcendental Functions. The Hypergeometric Function. Legendre Functions"

I feel like a Lovecraft protagonist.""",
409254: """Елена: * Contact dermatitis - inflammation of the skin caused by mechanical, physical or chemical irritants as a result of direct contact.
Today at my appointment I had a guy with contact dermatitis. ON HIS FOREHEAD!! His friends wrote the word D*CK on it in gouache.""",
409255: """xxx: I drowned my iPhone in the bathtub yesterday
yyy: and what happened to it?
xxx: works like a clock
xxx: literally, it only shows the time""",
409256: """ХХХ: The economy must be economical!
УУУ: and a horse must be conical""",
409257: """Recently took tests at the hospital.
The TV: Your urine can tell much more about you than you know yourself!
me: Need to urgently change the server password...""",
409258: """xxx: "The year turned out to be a tough one", - the admins write on the main page.
xxx: But the one that's come, 2011, is a simple one! Where's the joy, where's the joy? :)""",
409259: """JIeryxa: You won't believe it - I was unpacking a pack with liter cans of Tuborg Green and at the bottom under them lay a black sock :-D""",
409260: """a psychology test for physicists:
do you feel sorry for Schrödinger's cat?""",
409261: """xxx: you and I were born on the same day )
yyy: really??? what date are you?
xxx: *facepalm*""",
409262: """ххх: do you have iperf?
yyy: iperf? a hammer drill from Apple or something?""",
409263: """Talking to mom:

me: here, eat an apple
I: let's split it
mom: no, I won't eat it unwashed""",
409264: """<Magiqfish> joking about 3.14 is out of fashion, true nerds know a couple of dirty jokes about 100-105 digits after the point =D""",
409265: """xxx: Finally managed to kick my husband out to take out the trash.
xxx: Though to do that I had to stage a solar eclipse.""",
409266: """Айвар: And do you also have that friend who you send an ICQ message to, and you sit there waiting, waiting for an answer. You sit for half an hour, refreshed the contact list a hundred times already, refreshed Rambler, went and ate something, finally got sick of waiting, went to the couch.

Айвар: Laid down in the cozy little bed, wrapped up in the warm little blanket, put my head on the cool soft little pillow, just started falling asleep

Айвар: When from the computer side that bitch goes "UH-OH\"""",
409267: """Sorting exam tickets by rows:
"..Dirichlet Kernel.. Fejér Method. Fejér Kernel.."
And I just pictured the ticket task: "Reassemble the Dirichlet
Kernel\"...""",
409268: """Valdano: my parents accidentally found the New Year video of us boozing.
Valdano: thought I was done for. but they laughed and posted it on YouTube""",
409269: """xxx: give me a mag of cartridges, and the world will be better
yyy: one mag won't be enough
xxx: I didn't say it'd become perfect. But it'd become better""",
409270: """xxx: Our upstairs neighbors really miscalculated the wattage of their heated floors...
xxx: So much so that now we have a heated ceiling )""",
409271: """xxx: Why do I only break up with girls after I buy a big box of condoms?""",
409272: """Riding the bus, it's -30C outside, not much warmer inside. At one of the stops a schoolkid climbs in, scratching out the fare in small change with stiff fingers. A conductress auntie in a down jacket and felt boots comes up. Asks the kid - "Going far?", he says "getting off in a couple stops" and hands over the change. The conductress: "Ride for free. Better buy yourself some ICE CREAM!\"""",
409273: """FikuS: School break is when you wake up and an hour or two later it starts getting dark.""",
409274: """xxx: Every time I buy new slippers, the cat has a fit. Power has changed hands!""",
}

if __name__ == "__main__":
    import sys
    src = "/home/hatch/workspace/translation-work/bashim/chunks/chunk_27.jsonl"
    dst = "/home/hatch/workspace/translation-work/bashim/out/chunk_27.jsonl"
    start, end = 101, 200  # 1-based line numbers
    lines = open(src, encoding="utf-8").read().splitlines()
    assert len(lines) >= end, f"only {len(lines)} lines"
    # Build and validate everything BEFORE appending (avoids partial writes).
    out = []
    want_ids = []
    for ln in range(start, end + 1):
        r = json.loads(lines[ln - 1])
        en = EN.get(r["id"])
        assert en is not None, f"missing translation for id {r['id']} (line {ln})"
        assert en, f"empty en for id {r['id']}"
        assert en.count("\n") == r["text"].count("\n"), (
            f"newline mismatch id {r['id']}: en={en.count(chr(10))} ru={r['text'].count(chr(10))}")
        out.append(json.dumps({"id": r["id"], "ru": r["text"], "en": en}, ensure_ascii=False))
        want_ids.append(r["id"])
    # Resume safety: skip ids already present in dst.
    have = set()
    try:
        with open(dst, encoding="utf-8") as f:
            for l in f:
                l = l.strip()
                if l:
                    have.add(json.loads(l)["id"])
    except FileNotFoundError:
        pass
    new = [o for o, i in zip(out, want_ids) if i not in have]
    with open(dst, "a", encoding="utf-8") as f:
        for o in new:
            f.write(o + "\n")
    print(f"batch lines {start}-{end}: {len(new)} appended "
          f"({len(out) - len(new)} already present)")
