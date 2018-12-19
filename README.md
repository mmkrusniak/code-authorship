# code-authorship
Code authorship analysis project for LING 227 course.

## Where does the data go?
Data goes in the data/<author> folder. There's a different folder for each author. Make sure to add a corresponding out/<author> folder.

## Running the parser
Before you run the authorship analysis, you need to parse the data into JSON files. Just run the script ./json_parse and you'll be good to go.
(You do need Maven in order to run the parser.)

## Running the analysis
./analyze will run the authorship analysis. The syntax is:
`./analyze [MAXSIM|SVM|TSNE|BASE] [style|struc|all] <max_num_files> <number_cross_validations> <authors...>`
where:
- [MAXSIM|SVM|TSNE|BASE] denotes a mode. MAXSIM, SVM, and BASE run their respective models; TSNE prints out a graph of the flattened vectors.
- [style|struc|all] denotes what feature set you want to use.
- <max_num_files> denotes the number of files for each author you want to use. It's suggested that you make this the number of files in your smallest category.
- <number_cross_validations> denotes the number of cross validations to use when evaluating the model.
- <authors...> is a list of the authors/groups you'd like to analyze, according to the name of their data and out folders.

Example: ./analyze.sh SVM all 800 10 jigsaw jetty
(the optimal case for us!)
